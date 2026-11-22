package net.igng.bbs.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDetailDto(
    val field: String? = null,
    val issue: String? = null,
    val expected: String? = null,
)

@Serializable
data class ApiErrorBodyDto(
    val code: String = "server.internal_error",
    val message: String = "",
    val details: List<ApiErrorDetailDto> = emptyList(),
)

@Serializable
data class ApiErrorEnvelopeDto(
    val error: ApiErrorBodyDto = ApiErrorBodyDto(),
    @SerialName("request_id") val requestId: String? = null,
    val timestamp: String? = null,
)

class ApiException(
    val httpStatus: Int,
    val errorCode: String,
    val serverMessage: String,
    val requestId: String? = null,
    val retryAfterSeconds: Long? = null,
) : Exception(friendlyMessage(httpStatus, errorCode, serverMessage)) {

    val isUnauthorized: Boolean get() = httpStatus == 401
    val isForbidden: Boolean get() = httpStatus == 403
    val isNotFound: Boolean get() = httpStatus == 404
    val isRateLimited: Boolean get() = httpStatus == 429 || errorCode == "rate_limit.exceeded" || errorCode == "bbs.daily_limit_exceeded"
    val isNetworkOrServer: Boolean get() = httpStatus == 0 || httpStatus >= 500

    companion object {
        fun friendlyMessage(status: Int, code: String, serverMessage: String): String = when (code) {
            "auth.token_missing", "auth.token_expired", "auth.token_invalid", "auth.session_revoked" ->
                "登录状态已失效，请重新登录"
            "auth.insufficient_scope", "auth.insufficient_role" ->
                serverMessage.ifBlank { "当前账户没有执行该操作的权限" }
            "bbs.post_not_found" -> serverMessage.ifBlank { "文章不存在或已删除" }
            "bbs.post_forbidden" -> serverMessage.ifBlank { "你暂时无权查看或操作这篇文章" }
            "bbs.post_invalid_state" -> serverMessage.ifBlank { "内容当前状态不允许此操作" }
            "bbs.comment_not_found" -> serverMessage.ifBlank { "评论不存在或已删除" }
            "bbs.comment_blocked" -> serverMessage.ifBlank { "内容触发社区审核规则，请修改后重试" }
            "bbs.column_not_found" -> serverMessage.ifBlank { "专栏不存在" }
            "bbs.column_forbidden" -> serverMessage.ifBlank { "没有该专栏的操作权限" }
            "bbs.column_conflict" -> serverMessage.ifBlank { "专栏标识或状态冲突，请检查后重试" }
            "bbs.series_not_found" -> serverMessage.ifBlank { "系列不存在" }
            "bbs.series_forbidden" -> serverMessage.ifBlank { "没有该系列的管理权限" }
            "bbs.appeal_pending" -> "该内容已有进行中的申诉，请等待复核结果"
            "bbs.appeal_not_allowed" -> serverMessage.ifBlank { "当前处置暂不支持再次申诉" }
            "bbs.report_duplicate" -> "你已举报过这条内容，请等待处理"
            "bbs.daily_limit_exceeded" -> serverMessage.ifBlank { "今日操作次数已达上限，请稍后再试" }
            "rate_limit.exceeded" -> "请求过于频繁，请稍后再试"
            "media.too_large" -> "图片体积超出上限，请压缩后再上传"
            "media.invalid_format" -> "不支持的图片格式，请换一张再试"
            "media.quota_exceeded" -> "云端媒体空间已满，请先清理无用图片"
            "network.offline" -> "网络连接失败，请检查网络后重试"
            "network.timeout" -> "请求超时，请稍后重试"
            else -> serverMessage.ifBlank {
                when {
                    status == 0 -> "网络连接异常，请稍后重试"
                    status >= 500 -> "服务暂时不可用，请稍后重试"
                    else -> "操作未能完成，请稍后重试"
                }
            }
        }
    }
}

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val error: ApiException) : ApiResult<Nothing>
}

inline fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) block(data)
    return this
}

inline fun <T> ApiResult<T>.onFailure(block: (ApiException) -> Unit): ApiResult<T> {
    if (this is ApiResult.Failure) block(error)
    return this
}
