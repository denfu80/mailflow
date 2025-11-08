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

import android.annotation.SuppressLint

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
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_SYNC,
                    "Email Sync",
                    AndroidNotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Email synchronization status"
                },
                NotificationChannel(
                    CHANNEL_TODOS,
                    "New Todos",
                    AndroidNotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for newly created todos"
                    enableVibration(true)
                }
            )
            val systemNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            systemNotificationManager.createNotificationChannels(channels)
        }
    }

    @SuppressLint("MissingPermission")
    fun showTodoCreatedNotification(todoTitle: String, listName: String) {
        if (hasNotificationPermission()) {
            val notification = NotificationCompat.Builder(context, CHANNEL_TODOS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Todo Created")
                .setContentText("'$todoTitle' added to list '$listName'")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    @SuppressLint("MissingPermission")
    fun showSyncNotification(
        messagesFetched: Int,
        messagesProcessed: Int
    ) {
        if (hasNotificationPermission()) {
            val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Email sync complete")
                .setContentText("$messagesFetched fetched, $messagesProcessed processed")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID_SYNC, notification)
        }
    }

    @SuppressLint("MissingPermission")
    fun showSyncFailedNotification(error: String) {
        if (hasNotificationPermission()) {
            val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Email sync failed")
                .setContentText(error)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID_SYNC, notification)
        }
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
        private const val CHANNEL_TODOS = "channel_todos"
        private const val CHANNEL_SYNC = "channel_sync"
        private const val NOTIFICATION_ID_SYNC = 1003
    }
}

