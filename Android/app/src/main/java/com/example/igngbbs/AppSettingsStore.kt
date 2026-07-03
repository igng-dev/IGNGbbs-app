package com.example.igngbbs

import android.content.Context

class AppSettingsStore(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences("igngbbs_settings", Context.MODE_PRIVATE)

    fun notificationSettings(): NotificationSettings {
        return NotificationSettings(
            systemEnabled = preferences.getBoolean(KEY_NOTIFICATION_SYSTEM_ENABLED, true),
            replyEnabled = preferences.getBoolean(KEY_NOTIFICATION_REPLY_ENABLED, false),
            dailyEnabled = preferences.getBoolean(KEY_NOTIFICATION_DAILY_ENABLED, false)
        )
    }

    fun setNotificationSettings(settings: NotificationSettings) {
        preferences.edit()
            .putBoolean(KEY_NOTIFICATION_SYSTEM_ENABLED, settings.systemEnabled)
            .putBoolean(KEY_NOTIFICATION_REPLY_ENABLED, settings.replyEnabled)
            .putBoolean(KEY_NOTIFICATION_DAILY_ENABLED, settings.dailyEnabled)
            .apply()
    }

    fun notificationPermissionRequested(): Boolean {
        return preferences.getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, false)
    }

    fun setNotificationPermissionRequested(requested: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, requested).apply()
    }

    fun systemNotificationBaselineRecipientId(): Int {
        return preferences.getInt(KEY_SYSTEM_NOTIFICATION_BASELINE_RECIPIENT_ID, 0)
    }

    fun setSystemNotificationBaselineRecipientId(id: Int) {
        preferences.edit().putInt(KEY_SYSTEM_NOTIFICATION_BASELINE_RECIPIENT_ID, id).apply()
    }

    fun replyNotificationBaselineMentionId(): Int {
        return preferences.getInt(KEY_REPLY_NOTIFICATION_BASELINE_MENTION_ID, 0)
    }

    fun setReplyNotificationBaselineMentionId(id: Int) {
        preferences.edit().putInt(KEY_REPLY_NOTIFICATION_BASELINE_MENTION_ID, id).apply()
    }

    fun dailyReportLastSentDate(): String {
        return preferences.getString(KEY_DAILY_REPORT_LAST_SENT_DATE, "") ?: ""
    }

    fun setDailyReportLastSentDate(date: String) {
        preferences.edit().putString(KEY_DAILY_REPORT_LAST_SENT_DATE, date).apply()
    }

    fun dailyReportLastKnownDate(): String {
        return preferences.getString(KEY_DAILY_REPORT_LAST_KNOWN_DATE, "") ?: ""
    }

    fun setDailyReportLastKnownDate(date: String) {
        preferences.edit().putString(KEY_DAILY_REPORT_LAST_KNOWN_DATE, date).apply()
    }

    fun dailyReportLastKnownViews(): Int {
        return preferences.getInt(KEY_DAILY_REPORT_LAST_KNOWN_VIEWS, 0)
    }

    fun setDailyReportLastKnownViews(views: Int) {
        preferences.edit().putInt(KEY_DAILY_REPORT_LAST_KNOWN_VIEWS, views).apply()
    }

    fun hapticsEnabled(): Boolean {
        return preferences.getBoolean(KEY_HAPTICS_ENABLED, true)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    }

    fun notificationPromptHiddenUntil(): Long {
        return preferences.getLong(KEY_NOTIFICATION_PROMPT_HIDDEN_UNTIL, 0L)
    }

    fun setNotificationPromptHiddenUntil(timestamp: Long) {
        preferences.edit().putLong(KEY_NOTIFICATION_PROMPT_HIDDEN_UNTIL, timestamp).apply()
    }

    private companion object {
        const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        const val KEY_NOTIFICATION_PROMPT_HIDDEN_UNTIL = "notification_prompt_hidden_until"
        const val KEY_NOTIFICATION_SYSTEM_ENABLED = "notification_system_enabled"
        const val KEY_NOTIFICATION_REPLY_ENABLED = "notification_reply_enabled"
        const val KEY_NOTIFICATION_DAILY_ENABLED = "notification_daily_enabled"
        const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
        const val KEY_SYSTEM_NOTIFICATION_BASELINE_RECIPIENT_ID = "notification_system_baseline_recipient_id"
        const val KEY_REPLY_NOTIFICATION_BASELINE_MENTION_ID = "notification_reply_baseline_mention_id"
        const val KEY_DAILY_REPORT_LAST_SENT_DATE = "daily_report_last_sent_date"
        const val KEY_DAILY_REPORT_LAST_KNOWN_DATE = "daily_report_last_known_date"
        const val KEY_DAILY_REPORT_LAST_KNOWN_VIEWS = "daily_report_last_known_views"
    }
}

data class NotificationSettings(
    val systemEnabled: Boolean = true,
    val replyEnabled: Boolean = false,
    val dailyEnabled: Boolean = false
) {
    fun hasAnyEnabled(): Boolean = systemEnabled || replyEnabled || dailyEnabled
}
