package com.mailflow.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mailflow.data.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val newMailsChannel = NotificationChannel(
                CHANNEL_NEW_MAILS,
                "New Emails",
                AndroidNotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new emails"
                enableVibration(true)
            }

            val processingChannel = NotificationChannel(
                CHANNEL_PROCESSING,
                "Email Processing",
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background email processing status"
                enableVibration(false)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Email Sync",
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Email synchronization status"
                enableVibration(false)
            }

            val systemNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

            systemNotificationManager.createNotificationChannel(newMailsChannel)
            systemNotificationManager.createNotificationChannel(processingChannel)
            systemNotificationManager.createNotificationChannel(syncChannel)
        }
    }

    fun showNewMailsNotification(
        agentName: String,
        messageCount: Int,
        deepLinkIntent: Intent? = null
    ) {
        if (!hasNotificationPermission()) return

        val pendingIntent = deepLinkIntent?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_MAILS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New emails for $agentName")
            .setContentText("$messageCount new email${if (messageCount > 1) "s" else ""} received")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                pendingIntent?.let { setContentIntent(it) }
            }
            .build()

        notificationManager.notify(NOTIFICATION_ID_NEW_MAILS, notification)
    }

    fun showProcessingNotification(
        agentName: String,
        progress: Int,
        total: Int
    ) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_PROCESSING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Processing emails")
            .setContentText("$agentName: $progress/$total")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(total, progress, false)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_PROCESSING, notification)
    }

    fun showProcessingCompleteNotification(
        agentName: String,
        processedCount: Int
    ) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_PROCESSING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Processing complete")
            .setContentText("$agentName: $processedCount emails processed")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_PROCESSING, notification)
    }

    fun showSyncNotification(
        messagesFetched: Int,
        messagesProcessed: Int
    ) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Email sync complete")
            .setContentText("$messagesFetched fetched, $messagesProcessed processed")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_SYNC, notification)
    }

    fun showSyncFailedNotification(error: String) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Email sync failed")
            .setContentText(error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_SYNC, notification)
    }

    fun cancelProcessingNotification() {
        notificationManager.cancel(NOTIFICATION_ID_PROCESSING)
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    companion object {
        private const val CHANNEL_NEW_MAILS = "channel_new_mails"
        private const val CHANNEL_PROCESSING = "channel_processing"
        private const val CHANNEL_SYNC = "channel_sync"

        private const val NOTIFICATION_ID_NEW_MAILS = 1001
        private const val NOTIFICATION_ID_PROCESSING = 1002
        private const val NOTIFICATION_ID_SYNC = 1003
    }
}
