package net.igng.bbs.core.model

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class SavedAccount(
    val userId: Long,
    val username: String,
    val nickname: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAtEpochSec: Long,
    val refreshExpiresAtEpochSec: Long,
    val sessionId: String,
    val grantedScope: String = "",
) {
    val displayName: String get() = nickname.ifBlank { username }
    val publicUserId: String get() = "usr_$userId"

    fun isAccessTokenExpiringSoon(nowEpochSec: Long = Instant.now().epochSecond, leewaySec: Long = 90L): Boolean =
        accessExpiresAtEpochSec <= nowEpochSec + leewaySec

    fun isRefreshExpired(nowEpochSec: Long = Instant.now().epochSecond): Boolean =
        refreshExpiresAtEpochSec <= nowEpochSec
}

@Serializable
data class OAuthPendingRequest(
    val state: String,
    val codeVerifier: String,
    val createdAtEpochSec: Long,
    val forceSwitchAccount: Boolean = false,
) {
    fun isExpired(nowEpochSec: Long = Instant.now().epochSecond, ttlSec: Long = 900L): Boolean =
        nowEpochSec - createdAtEpochSec > ttlSec
}

object V1Id {
    fun numericOf(raw: String?): Int? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null
        val tail = value.substringAfter('_', value)
        return tail.toIntOrNull()
    }

    fun post(numericId: Int): String = "post_$numericId"
    fun user(numericId: Int): String = "usr_$numericId"
    fun column(numericId: Int): String = "col_$numericId"
    fun series(numericId: Int): String = "ser_$numericId"
    fun comment(numericId: Int): String = "cmt_$numericId"
}

object BbsFormatters {
    private val dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())
    private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日")
        .withZone(ZoneId.systemDefault())
    private val fullDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())

    fun compactCount(value: Int): String = when {
        value >= 100_000 -> "${value / 10_000}万"
        value >= 10_000 -> String.format("%.1f万", value / 10_000.0)
        value >= 1_000 -> String.format("%.1fk", value / 1_000.0)
        else -> value.toString()
    }

    fun relativeTime(isoUtc: String?, now: Instant = Instant.now()): String {
        if (isoUtc.isNullOrBlank()) return ""
        val instant = runCatching { Instant.parse(isoUtc) }.getOrNull() ?: return isoUtc
        val seconds = Duration.between(instant, now).seconds
        return when {
            seconds < 60 -> "刚刚"
            seconds < 3600 -> "${seconds / 60} 分钟前"
            seconds < 86_400 -> "${seconds / 3600} 小时前"
            seconds < 86_400 * 7 -> "${seconds / 86_400} 天前"
            seconds < 86_400 * 300 -> monthDayFormatter.format(instant)
            else -> dateOnlyFormatter.format(instant)
        }
    }

    fun fullDateTime(isoUtc: String?): String {
        if (isoUtc.isNullOrBlank()) return ""
        val instant = runCatching { Instant.parse(isoUtc) }.getOrNull() ?: return isoUtc
        return fullDateTimeFormatter.format(instant)
    }

    fun formatBytes(bytes: Long): String = when {
        bytes >= 1024L * 1024L * 1024L -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024L * 1024L -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> String.format("%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
    }

    fun readingMinutesLabel(minutes: Int): String = "约 ${minutes.coerceAtLeast(1)} 分钟阅读"

    fun postStatusLabel(status: Int, banStatus: Int, moderationStatus: String? = null): String = when {
        status == 3 -> "已删除"
        status == 1 -> "草稿"
        banStatus == 2 -> "已禁止查看"
        banStatus == 4 -> "受限制"
        moderationStatus == "pending" -> "审核中"
        moderationStatus == "rejected" -> "审核未通过"
        banStatus == 1 -> "仅作者可见"
        else -> "已发布"
    }

    fun visibilityLabel(visibility: String): String = when (visibility.lowercase()) {
        "public" -> "公开"
        "followers" -> "仅粉丝可见"
        "following" -> "仅关注可见"
        "private" -> "私密"
        else -> visibility
    }
}
