package net.igng.bbs.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.igng.bbs.core.model.SiteEndpoints
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.TimeUnit

object IgngJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }
}

interface AuthTokenProvider {
    fun currentAccessToken(): String?
    suspend fun refreshAccessToken(failedAccessToken: String?): String?
    fun onSessionInvalidated(reason: String)
}

class SessionExpiredNotifier {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun notifyExpired(reason: String = "登录状态已失效，请重新登录") {
        _events.tryEmit(reason)
    }
}

class IgngHttpClient(
    val endpoints: SiteEndpoints,
    private val appVersion: String,
    private val deviceIdProvider: () -> String,
    private val authTokenProvider: AuthTokenProvider,
    val sessionExpiredNotifier: SessionExpiredNotifier,
    val json: Json = IgngJson.instance,
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val urlStr = original.url.toString()
            val builder = original.newBuilder()
                .header("User-Agent", "IGNGbbs-Android/$appVersion")
                .header("X-Client-Id", SiteEndpoints.CLIENT_ID)
                .header("X-Client-Version", appVersion)
                .header("X-Device-Id", deviceIdProvider())
                .header("X-Request-Id", "req_${UUID.randomUUID().toString().replace("-", "").take(16)}")
            if (original.header("Accept").isNullOrBlank()) {
                builder.header("Accept", "application/json")
            }
            if (endpoints.isTrustedAuthHost(urlStr) && original.header("Authorization").isNullOrBlank()) {
                authTokenProvider.currentAccessToken()?.takeIf { it.isNotBlank() }?.let { token ->
                    builder.header("Authorization", "Bearer $token")
                }
            }
            chain.proceed(builder.build())
        }
        .build()

    /**
     * Separate OkHttpClient for Coil image loading: attaches Bearer + X-Client-* ONLY to trusted
     * IGNG hosts with `Accept: image/*, */*;q=0.8` so private/draft BBS media loads seamlessly.
     */
    val mediaOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val urlStr = original.url.toString()
            if (!endpoints.isTrustedAuthHost(urlStr)) {
                return@addInterceptor chain.proceed(original)
            }
            val builder = original.newBuilder()
                .header("User-Agent", "IGNGbbs-Android/$appVersion")
                .header("X-Client-Id", SiteEndpoints.CLIENT_ID)
                .header("X-Client-Version", appVersion)
                .header("X-Device-Id", deviceIdProvider())
                .header("Accept", "image/webp,image/*,*/*;q=0.8")
            authTokenProvider.currentAccessToken()?.takeIf { it.isNotBlank() }?.let { token ->
                builder.header("Authorization", "Bearer $token")
            }
            chain.proceed(builder.build())
        }
        .build()

    suspend inline fun <reified T> get(
        path: String,
        query: Map<String, String?> = emptyMap(),
        requireAuth: Boolean = false,
    ): ApiResult<T> = executeJson(
        method = "GET",
        path = path,
        query = query,
        bodyJson = null,
        deserializer = json.serializersModule.serializer(),
        requireAuth = requireAuth,
    )

    suspend inline fun <reified Req, reified Res> post(
        path: String,
        body: Req,
        idempotencyKey: String = "idem_${UUID.randomUUID().toString().replace("-", "")}",
        requireAuth: Boolean = true,
    ): ApiResult<Res> {
        val serializer: KSerializer<Req> = json.serializersModule.serializer()
        val encoded = json.encodeToString(serializer, body)
        return executeJson(
            method = "POST",
            path = path,
            query = emptyMap(),
            bodyJson = encoded,
            deserializer = json.serializersModule.serializer(),
            idempotencyKey = idempotencyKey,
            requireAuth = requireAuth,
        )
    }

    suspend inline fun <reified Res> postEmpty(
        path: String,
        idempotencyKey: String = "idem_${UUID.randomUUID().toString().replace("-", "")}",
        requireAuth: Boolean = true,
    ): ApiResult<Res> = executeJson(
        method = "POST",
        path = path,
        query = emptyMap(),
        bodyJson = "{}",
        deserializer = json.serializersModule.serializer(),
        idempotencyKey = idempotencyKey,
        requireAuth = requireAuth,
    )

    suspend inline fun <reified Req, reified Res> patch(
        path: String,
        body: Req,
        ifMatch: String? = null,
    ): ApiResult<Res> {
        val serializer: KSerializer<Req> = json.serializersModule.serializer()
        val encoded = json.encodeToString(serializer, body)
        return executeJson(
            method = "PATCH",
            path = path,
            query = emptyMap(),
            bodyJson = encoded,
            deserializer = json.serializersModule.serializer(),
            ifMatch = ifMatch,
            requireAuth = true,
        )
    }

    suspend fun deleteNoContent(path: String): ApiResult<Unit> = executeUnit(
        method = "DELETE",
        path = path,
    )

    suspend fun postNoContent(
        path: String,
        bodyJson: String? = null,
        idempotencyKey: String = "idem_${UUID.randomUUID().toString().replace("-", "")}",
    ): ApiResult<Unit> = executeUnit(
        method = "POST",
        path = path,
        bodyJson = bodyJson,
        idempotencyKey = idempotencyKey,
    )

    suspend inline fun <reified Res> uploadMultipart(
        path: String,
        fieldName: String = "file",
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        extraFields: Map<String, String> = emptyMap(),
        idempotencyKey: String = "idem_${UUID.randomUUID().toString().replace("-", "")}",
    ): ApiResult<Res> = executeMultipart(
        path = path,
        fieldName = fieldName,
        fileName = fileName,
        mimeType = mimeType,
        bytes = bytes,
        extraFields = extraFields,
        deserializer = json.serializersModule.serializer(),
        idempotencyKey = idempotencyKey,
    )

    fun buildUrl(path: String, query: Map<String, String?> = emptyMap()): String {
        val cleanPath = if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            val normalized = if (path.startsWith("/")) path else "/$path"
            "${endpoints.apiV1Base}$normalized"
        }
        val activeParams = query.entries.filter { !it.value.isNullOrBlank() }
        if (activeParams.isEmpty()) return cleanPath
        val encoded = activeParams.joinToString("&") { (k, v) ->
            "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v!!, "UTF-8")}"
        }
        return if ('?' in cleanPath) "$cleanPath&$encoded" else "$cleanPath?$encoded"
    }

    suspend fun <T> executeJson(
        method: String,
        path: String,
        query: Map<String, String?> = emptyMap(),
        bodyJson: String? = null,
        deserializer: DeserializationStrategy<T>,
        idempotencyKey: String? = null,
        ifMatch: String? = null,
        requireAuth: Boolean = false,
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        val url = buildUrl(path, query)
        val maxAttempts = if (method == "GET") 2 else 1
        var attempt = 0
        var lastError: ApiException? = null

        while (attempt < maxAttempts) {
            attempt++
            val tokenBefore = authTokenProvider.currentAccessToken()
            if (requireAuth && tokenBefore.isNullOrBlank()) {
                return@withContext ApiResult.Failure(
                    ApiException(401, "auth.token_missing", "请先登录 IGNG 账户"),
                )
            }
            val request = buildRequest(method, url, bodyJson, idempotencyKey, ifMatch, tokenBefore)
            when (val res = runSingle(request, deserializer)) {
                is ApiResult.Success -> return@withContext res
                is ApiResult.Failure -> {
                    val err = res.error
                    if (err.isUnauthorized && !tokenBefore.isNullOrBlank()) {
                        val refreshed = authTokenProvider.refreshAccessToken(tokenBefore)
                        if (!refreshed.isNullOrBlank()) {
                            val retryReq = buildRequest(method, url, bodyJson, idempotencyKey, ifMatch, refreshed)
                            return@withContext runSingle(retryReq, deserializer)
                        } else {
                            authTokenProvider.onSessionInvalidated(err.message ?: "登录状态已失效")
                            sessionExpiredNotifier.notifyExpired(err.message ?: "登录状态已失效，请重新登录")
                            return@withContext res
                        }
                    }
                    lastError = err
                    if (method == "GET" && err.isNetworkOrServer && attempt < maxAttempts) {
                        delay(350L * attempt)
                        continue
                    }
                    return@withContext res
                }
            }
        }
        ApiResult.Failure(lastError ?: ApiException(0, "network.offline", "网络请求失败"))
    }

    suspend fun executeUnit(
        method: String,
        path: String,
        bodyJson: String? = null,
        idempotencyKey: String? = null,
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        val url = buildUrl(path)
        val tokenBefore = authTokenProvider.currentAccessToken()
        val request = buildRequest(method, url, bodyJson, idempotencyKey, null, tokenBefore)
        val first = runSingleUnit(request)
        if (first is ApiResult.Failure && first.error.isUnauthorized && !tokenBefore.isNullOrBlank()) {
            val refreshed = authTokenProvider.refreshAccessToken(tokenBefore)
            if (!refreshed.isNullOrBlank()) {
                return@withContext runSingleUnit(buildRequest(method, url, bodyJson, idempotencyKey, null, refreshed))
            }
            authTokenProvider.onSessionInvalidated(first.error.message ?: "登录状态已失效")
            sessionExpiredNotifier.notifyExpired(first.error.message ?: "登录状态已失效，请重新登录")
        }
        first
    }

    suspend fun <T> executeMultipart(
        path: String,
        fieldName: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        extraFields: Map<String, String>,
        deserializer: DeserializationStrategy<T>,
        idempotencyKey: String,
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        val url = buildUrl(path)
        fun makeReq(token: String?): Request {
            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .apply {
                    extraFields.forEach { (k, v) -> addFormDataPart(k, v) }
                    addFormDataPart(fieldName, fileName, bytes.toRequestBody(mimeType.toMediaType()))
                }
                .build()
            return Request.Builder()
                .url(url)
                .post(multipart)
                .header("Idempotency-Key", idempotencyKey)
                .apply { if (!token.isNullOrBlank()) header("Authorization", "Bearer $token") }
                .build()
        }
        val tokenBefore = authTokenProvider.currentAccessToken()
        val first = runSingle(makeReq(tokenBefore), deserializer)
        if (first is ApiResult.Failure && first.error.isUnauthorized && !tokenBefore.isNullOrBlank()) {
            val refreshed = authTokenProvider.refreshAccessToken(tokenBefore)
            if (!refreshed.isNullOrBlank()) {
                return@withContext runSingle(makeReq(refreshed), deserializer)
            }
            authTokenProvider.onSessionInvalidated(first.error.message ?: "登录状态已失效")
            sessionExpiredNotifier.notifyExpired(first.error.message ?: "登录状态已失效，请重新登录")
        }
        first
    }

    private fun buildRequest(
        method: String,
        url: String,
        bodyJson: String?,
        idempotencyKey: String?,
        ifMatch: String?,
        accessToken: String?,
    ): Request {
        val builder = Request.Builder().url(url)
        if (!accessToken.isNullOrBlank()) builder.header("Authorization", "Bearer $accessToken")
        if (!idempotencyKey.isNullOrBlank() && method == "POST") builder.header("Idempotency-Key", idempotencyKey)
        if (!ifMatch.isNullOrBlank()) builder.header("If-Match", ifMatch)
        when (method) {
            "GET" -> builder.get()
            "DELETE" -> builder.delete()
            "POST" -> builder.post((bodyJson ?: "{}").toRequestBody(jsonMediaType))
            "PATCH" -> builder.patch((bodyJson ?: "{}").toRequestBody(jsonMediaType))
            else -> builder.method(method, bodyJson?.toRequestBody(jsonMediaType))
        }
        return builder.build()
    }

    private fun <T> runSingle(request: Request, deserializer: DeserializationStrategy<T>): ApiResult<T> {
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val parsed = json.decodeFromString(deserializer, raw)
                    ApiResult.Success(parsed)
                } else {
                    ApiResult.Failure(parseError(response.code, raw, response.header("Retry-After")))
                }
            }
        } catch (e: SocketTimeoutException) {
            ApiResult.Failure(ApiException(0, "network.timeout", "请求超时"))
        } catch (e: IOException) {
            ApiResult.Failure(ApiException(0, "network.offline", "网络连接失败"))
        } catch (e: Exception) {
            ApiResult.Failure(ApiException(0, "client.parse_error", "数据解析失败：${e.message ?: "格式异常"}"))
        }
    }

    private fun runSingleUnit(request: Request): ApiResult<Unit> {
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Failure(parseError(response.code, raw, response.header("Retry-After")))
                }
            }
        } catch (e: SocketTimeoutException) {
            ApiResult.Failure(ApiException(0, "network.timeout", "请求超时"))
        } catch (e: IOException) {
            ApiResult.Failure(ApiException(0, "network.offline", "网络连接失败"))
        } catch (e: Exception) {
            ApiResult.Failure(ApiException(0, "client.unknown", e.message ?: "请求失败"))
        }
    }

    private fun parseError(code: Int, raw: String, retryAfterHeader: String?): ApiException {
        val envelope = runCatching { json.decodeFromString(ApiErrorEnvelopeDto.serializer(), raw) }.getOrNull()
        val errCode = envelope?.error?.code?.ifBlank { "http.$code" } ?: "http.$code"
        val errMsg = envelope?.error?.message.orEmpty()
        return ApiException(
            httpStatus = code,
            errorCode = errCode,
            serverMessage = errMsg,
            requestId = envelope?.requestId,
            retryAfterSeconds = retryAfterHeader?.toLongOrNull(),
        )
    }
}
