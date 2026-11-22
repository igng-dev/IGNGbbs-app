package net.igng.bbs.core.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.igng.bbs.core.model.OAuthPendingRequest
import net.igng.bbs.core.model.SavedAccount
import net.igng.bbs.core.model.SiteEndpoints
import net.igng.bbs.core.network.AuthTokenProvider
import net.igng.bbs.core.network.IgngJson
import net.igng.bbs.core.network.SingleFlightTokenRefresher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.util.concurrent.TimeUnit

@Serializable
private data class OAuthTokenUserDto(
    val id: Long = 0L,
    val username: String = "",
    val nickname: String = "",
    val email: String? = null,
)

@Serializable
private data class OAuthTokenResponseDto(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("expires_in") val expiresIn: Long = 3600L,
    @SerialName("refresh_expires_at") val refreshExpiresAt: String? = null,
    @SerialName("session_id") val sessionId: String = "",
    val scope: String = "",
    val user: OAuthTokenUserDto = OAuthTokenUserDto(),
)

@Serializable
private data class OAuthExchangeBody(
    @SerialName("grant_type") val grantType: String,
    @SerialName("client_id") val clientId: String,
    val code: String? = null,
    @SerialName("redirect_uri") val redirectUri: String? = null,
    @SerialName("code_verifier") val codeVerifier: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
)

sealed interface OAuthCallbackResult {
    data class Success(val account: SavedAccount) : OAuthCallbackResult
    data class Error(val message: String) : OAuthCallbackResult
    data object Ignored : OAuthCallbackResult
}

/**
 * First-party OAuth 2.1 Authorization Code + PKCE S256 manager for `igng_bbs_android`.
 * Launches system Custom Tabs (zero password collection in WebView), exchanges auth code for
 * rotating access/refresh tokens bound to an Auth device session, and coordinates concurrent
 * refresh calls through [SingleFlightTokenRefresher].
 */
class OAuthManager(
    private val endpoints: SiteEndpoints,
    private val appVersion: String,
    val sessionStore: SecureSessionStore,
) : AuthTokenProvider {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val oauthClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val refresher = SingleFlightTokenRefresher(
        currentAccessToken = { sessionStore.currentAccessToken() },
        performRefresh = { failedToken -> performRefreshTokenRotation(failedToken) },
    )

    fun launchLogin(context: Context, forceSwitchAccount: Boolean = false) {
        val verifier = PkceGenerator.generateCodeVerifier()
        val challenge = PkceGenerator.generateCodeChallenge(verifier)
        val state = PkceGenerator.generateState()
        sessionStore.savePendingOAuth(
            OAuthPendingRequest(
                state = state,
                codeVerifier = verifier,
                createdAtEpochSec = Instant.now().epochSecond,
                forceSwitchAccount = forceSwitchAccount,
            ),
        )
        val uriBuilder = Uri.parse(endpoints.oauthAuthorizeUrl).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", SiteEndpoints.CLIENT_ID)
            .appendQueryParameter("redirect_uri", SiteEndpoints.OAUTH_REDIRECT_URI)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
        if (forceSwitchAccount) {
            uriBuilder.appendQueryParameter("prompt", "login")
        }
        val authUri = uriBuilder.build()
        runCatching {
            val customTabs = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabs.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            customTabs.launchUrl(context, authUri)
        }.onFailure {
            val fallback = Intent(Intent.ACTION_VIEW, authUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallback)
        }
    }

    suspend fun handleCallbackUri(uri: Uri?): OAuthCallbackResult {
        if (uri == null) return OAuthCallbackResult.Ignored
        if (uri.scheme != "igngbbs" || uri.host != "auth" || uri.path != "/callback") {
            return OAuthCallbackResult.Ignored
        }
        val error = uri.getQueryParameter("error")
        if (!error.isNullOrBlank()) {
            return OAuthCallbackResult.Error(
                if (error == "access_denied") "你取消了登录授权" else "授权未完成（$error）",
            )
        }
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        if (code.isNullOrBlank() || state.isNullOrBlank()) {
            return OAuthCallbackResult.Error("授权回调参数不完整")
        }
        val pending = sessionStore.consumePendingOAuth(state)
            ?: return OAuthCallbackResult.Error("授权状态已过期或不匹配，请重新发起登录")

        return exchangeCodeForTokens(code = code, verifier = pending.codeVerifier)
    }

    private suspend fun exchangeCodeForTokens(code: String, verifier: String): OAuthCallbackResult =
        withContext(Dispatchers.IO) {
            val payload = OAuthExchangeBody(
                grantType = "authorization_code",
                clientId = SiteEndpoints.CLIENT_ID,
                code = code,
                redirectUri = SiteEndpoints.OAUTH_REDIRECT_URI,
                codeVerifier = verifier,
            )
            val bodyJson = IgngJson.instance.encodeToString(OAuthExchangeBody.serializer(), payload)
            val req = Request.Builder()
                .url(endpoints.oauthTokenUrl)
                .header("User-Agent", "IGNGbbs-Android/$appVersion")
                .header("Accept", "application/json")
                .post(bodyJson.toRequestBody(jsonMediaType))
                .build()
            runCatching {
                oauthClient.newCall(req).execute().use { res ->
                    val raw = res.body?.string().orEmpty()
                    if (!res.isSuccessful) {
                        return@withContext OAuthCallbackResult.Error("交换登录凭据失败（HTTP ${res.code}）")
                    }
                    val tokenRes = IgngJson.instance.decodeFromString(OAuthTokenResponseDto.serializer(), raw)
                    if (tokenRes.accessToken.isBlank() || tokenRes.refreshToken.isBlank()) {
                        return@withContext OAuthCallbackResult.Error("服务端未返回有效访问令牌")
                    }
                    val nowSec = Instant.now().epochSecond
                    val refreshExpSec = tokenRes.refreshExpiresAt
                        ?.let { runCatching { Instant.parse(it).epochSecond }.getOrNull() }
                        ?: (nowSec + 30L * 24L * 3600L)
                    val account = SavedAccount(
                        userId = tokenRes.user.id,
                        username = tokenRes.user.username,
                        nickname = tokenRes.user.nickname.ifBlank { tokenRes.user.username },
                        email = tokenRes.user.email,
                        accessToken = tokenRes.accessToken,
                        refreshToken = tokenRes.refreshToken,
                        accessExpiresAtEpochSec = nowSec + tokenRes.expiresIn.coerceAtLeast(60L),
                        refreshExpiresAtEpochSec = refreshExpSec,
                        sessionId = tokenRes.sessionId,
                        grantedScope = tokenRes.scope,
                    )
                    sessionStore.upsertAccount(account, makeActive = true)
                    OAuthCallbackResult.Success(account)
                }
            }.getOrElse {
                OAuthCallbackResult.Error("网络异常，无法完成登录授权")
            }
        }

    override fun currentAccessToken(): String? = sessionStore.currentAccessToken()

    override suspend fun refreshAccessToken(failedAccessToken: String?): String? =
        refresher.refreshIfNeeded(failedAccessToken)

    override fun onSessionInvalidated(reason: String) {
        sessionStore.clearActiveAccount()
    }

    private suspend fun performRefreshTokenRotation(failedAccessToken: String?): String? =
        withContext(Dispatchers.IO) {
            val active = sessionStore.activeAccount.value ?: return@withContext null
            if (failedAccessToken != null && active.accessToken != failedAccessToken) {
                return@withContext active.accessToken
            }
            if (active.refreshToken.isBlank() || active.isRefreshExpired()) {
                return@withContext null
            }
            val payload = OAuthExchangeBody(
                grantType = "refresh_token",
                clientId = SiteEndpoints.CLIENT_ID,
                refreshToken = active.refreshToken,
            )
            val bodyJson = IgngJson.instance.encodeToString(OAuthExchangeBody.serializer(), payload)
            val req = Request.Builder()
                .url(endpoints.oauthTokenUrl)
                .header("User-Agent", "IGNGbbs-Android/$appVersion")
                .header("Accept", "application/json")
                .post(bodyJson.toRequestBody(jsonMediaType))
                .build()
            runCatching {
                oauthClient.newCall(req).execute().use { res ->
                    if (!res.isSuccessful) return@withContext null
                    val raw = res.body?.string().orEmpty()
                    val tokenRes = IgngJson.instance.decodeFromString(OAuthTokenResponseDto.serializer(), raw)
                    if (tokenRes.accessToken.isBlank() || tokenRes.refreshToken.isBlank()) return@withContext null
                    val nowSec = Instant.now().epochSecond
                    val refreshExpSec = tokenRes.refreshExpiresAt
                        ?.let { runCatching { Instant.parse(it).epochSecond }.getOrNull() }
                        ?: active.refreshExpiresAtEpochSec
                    val rotated = active.copy(
                        username = tokenRes.user.username.ifBlank { active.username },
                        nickname = tokenRes.user.nickname.ifBlank { active.nickname },
                        email = tokenRes.user.email ?: active.email,
                        accessToken = tokenRes.accessToken,
                        refreshToken = tokenRes.refreshToken,
                        accessExpiresAtEpochSec = nowSec + tokenRes.expiresIn.coerceAtLeast(60L),
                        refreshExpiresAtEpochSec = refreshExpSec,
                        sessionId = tokenRes.sessionId.ifBlank { active.sessionId },
                        grantedScope = tokenRes.scope.ifBlank { active.grantedScope },
                    )
                    sessionStore.upsertAccount(rotated, makeActive = true)
                    rotated.accessToken
                }
            }.getOrNull()
        }

    suspend fun logoutActiveAccount() = withContext(Dispatchers.IO) {
        val active = sessionStore.activeAccount.value
        if (active != null && active.accessToken.isNotBlank()) {
            runCatching {
                val req = Request.Builder()
                    .url("${endpoints.apiV1Base}/auth/logout")
                    .header("User-Agent", "IGNGbbs-Android/$appVersion")
                    .header("X-Client-Id", SiteEndpoints.CLIENT_ID)
                    .header("X-Client-Version", appVersion)
                    .header("X-Device-Id", sessionStore.getOrCreateDeviceId())
                    .header("Accept", "application/json")
                    .header("Idempotency-Key", "logout_${System.currentTimeMillis()}")
                    .header("Authorization", "Bearer ${active.accessToken}")
                    .post("{}".toRequestBody(jsonMediaType))
                    .build()
                oauthClient.newCall(req).execute().close()
            }
        }
        sessionStore.clearActiveAccount()
    }
}
