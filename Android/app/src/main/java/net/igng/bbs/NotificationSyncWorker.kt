package net.igng.bbs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import net.igng.bbs.core.network.ApiResult
import java.util.concurrent.TimeUnit

class NotificationSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? BbsApplication ?: return Result.success()
        val container = app.container
        val prefs = container.settingsRepository.preferences.first()
        if (!prefs.backgroundNotificationsEnabled) return Result.success()
        val account = container.sessionStore.activeAccount.value ?: return Result.success()

        return when (val res = container.inboxRepository.listInbox(tab = "all", page = 1, pageSize = 5, unreadOnly = true)) {
            is ApiResult.Success -> {
                val newest = res.data.data.firstOrNull() ?: return Result.success()
                val lastNotified = container.settingsRepository.getLastNotifiedInboxId(account.userId)
                if (newest.id != lastNotified) {
                    container.settingsRepository.setLastNotifiedInboxId(account.userId, newest.id)
                    showNotification(
                        context = applicationContext,
                        title = newest.title.ifBlank { "IGNG BBS 新消息" },
                        body = newest.content.ifBlank { "你有 ${res.data.unread.total} 条未读社区消息" },
                        deepLinkUri = if (!newest.postId.isNullOrBlank()) {
                            Uri.parse("igngbbs://post/${newest.postId}")
                        } else {
                            Uri.parse("igngbbs://open/inbox")
                        },
                    )
                }
                Result.success()
            }
            is ApiResult.Failure -> Result.retry()
        }
    }

    private fun showNotification(context: Context, title: String, body: String, deepLinkUri: Uri) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "社区消息通知",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "接收 @提及、评论回复、系统通知与专栏动态"
        }
        manager.createNotificationChannel(channel)

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = deepLinkUri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            deepLinkUri.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val WORK_NAME = "igngbbs_inbox_sync"
        private const val CHANNEL_ID = "igngbbs_inbox_channel"
        private const val NOTIFICATION_ID = 2001

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val req = PeriodicWorkRequestBuilder<NotificationSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                req,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
