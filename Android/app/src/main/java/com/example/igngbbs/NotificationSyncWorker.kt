package com.example.igngbbs

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NotificationSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val settingsStore = AppSettingsStore(context)
        val sessionStore = SecureSessionStore(context)
        val session = sessionStore.cachedSession() ?: return Result.success()
        val settings = settingsStore.notificationSettings()
        if (!settings.hasAnyEnabled() || !AppNotificationManager.hasPermission(context)) {
            return Result.success()
        }
        val apiClient = BbsApiClient(BuildConfig.IGNG_BBS_BASE_URL) { session.sessionToken }
        return runCatching {
            withContext(Dispatchers.IO) {
                if (settings.systemEnabled) {
                    syncSystemNotifications(context, apiClient, settingsStore)
                }
                if (settings.replyEnabled) {
                    syncReplyNotifications(context, apiClient, settingsStore)
                }
                if (settings.dailyEnabled) {
                    syncDailyReport(context, apiClient, settingsStore)
                }
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    private fun syncSystemNotifications(
        context: Context,
        apiClient: BbsApiClient,
        settingsStore: AppSettingsStore
    ) {
        val items = apiClient.fetchNotifications("20", true).items
        val maxId = items.maxOfOrNull { it.id } ?: 0
        val baseline = settingsStore.systemNotificationBaselineRecipientId()
        if (baseline <= 0) {
            settingsStore.setSystemNotificationBaselineRecipientId(maxId)
            return
        }
        items
            .filter { it.id > baseline && it.isUnread() }
            .sortedBy { it.id }
            .forEach { item ->
                AppNotificationManager.showNotification(
                    context,
                    10_000 + item.id,
                    item.title.ifBlank { "新的系统通知" },
                    item.content.ifBlank { "你收到了一条新的系统通知" }
                )
            }
        if (maxId > baseline) {
            settingsStore.setSystemNotificationBaselineRecipientId(maxId)
        }
    }

    private fun syncReplyNotifications(
        context: Context,
        apiClient: BbsApiClient,
        settingsStore: AppSettingsStore
    ) {
        val items = apiClient.fetchCreatorComments("received", 20)
        val unreadItems = items.filter { it.mentionUnread && it.mentionId > 0 }
        val maxId = unreadItems.maxOfOrNull { it.mentionId } ?: 0
        val baseline = settingsStore.replyNotificationBaselineMentionId()
        if (baseline <= 0) {
            settingsStore.setReplyNotificationBaselineMentionId(maxId)
            return
        }
        unreadItems
            .filter { it.mentionId > baseline }
            .sortedBy { it.mentionId }
            .forEach { item ->
                val author = item.authorName.ifBlank { "有用户" }
                val title = if (item.postTitle.isNotBlank()) {
                    "$author 回复了《${item.postTitle}》"
                } else {
                    "$author 回复了你"
                }
                AppNotificationManager.showNotification(
                    context,
                    20_000 + item.mentionId,
                    title,
                    item.content.ifBlank { "点击查看回复内容" }
                )
            }
        if (maxId > baseline) {
            settingsStore.setReplyNotificationBaselineMentionId(maxId)
        }
    }

    private fun syncDailyReport(
        context: Context,
        apiClient: BbsApiClient,
        settingsStore: AppSettingsStore
    ) {
        val overview = apiClient.fetchCreatorOverview()
        val todayViews = overview.todayViews
        val todayKey = java.time.LocalDate.now().toString()
        val lastKnownDate = settingsStore.dailyReportLastKnownDate()
        val lastSentDate = settingsStore.dailyReportLastSentDate()
        if (lastKnownDate != todayKey) {
            settingsStore.setDailyReportLastKnownDate(todayKey)
            settingsStore.setDailyReportLastKnownViews(todayViews)
            if (lastSentDate != todayKey) {
                AppNotificationManager.showNotification(
                    context,
                    30_000,
                    "今日浏览日报",
                    "你今天的文章浏览量为 $todayViews"
                )
                settingsStore.setDailyReportLastSentDate(todayKey)
            }
            return
        }
        val lastViews = settingsStore.dailyReportLastKnownViews()
        if (lastSentDate != todayKey && todayViews != lastViews) {
            AppNotificationManager.showNotification(
                context,
                30_000,
                "今日浏览日报",
                "你今天的文章浏览量更新为 $todayViews"
            )
            settingsStore.setDailyReportLastSentDate(todayKey)
        }
        settingsStore.setDailyReportLastKnownViews(todayViews)
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "igngbbs_notification_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<NotificationSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
