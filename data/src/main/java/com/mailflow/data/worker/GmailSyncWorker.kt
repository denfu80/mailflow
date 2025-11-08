package com.mailflow.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mailflow.data.notification.NotificationManager
import com.mailflow.domain.model.SyncResult
import com.mailflow.domain.usecase.SyncEmailsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class GmailSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEmailsUseCase: SyncEmailsUseCase,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            when (val syncResult = syncEmailsUseCase()) {
                is SyncResult.Success -> {
                    if (syncResult.messagesProcessed > 0) {
                        notificationManager.showSyncNotification(
                            messagesFetched = syncResult.messagesFetched,
                            messagesProcessed = syncResult.messagesProcessed
                        )
                    }
                    Result.success()
                }

                is SyncResult.Failure -> {
                    if (runAttemptCount >= MAX_RETRY_ATTEMPTS - 1) {
                        notificationManager.showSyncFailedNotification(syncResult.error.message)
                    }
                    Result.retry()
                }
                // PartialSuccess is not handled in the simplified version
                else -> Result.failure()
            }
        } catch (e: Exception) {
            if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                Result.failure(
                    workDataOf(KEY_ERROR_MESSAGE to "Failed after $MAX_RETRY_ATTEMPTS attempts: ${e.message}")
                )
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        const val WORK_NAME = "gmail_sync_work"
        const val KEY_ERROR_MESSAGE = "error_message"
        const val MAX_RETRY_ATTEMPTS = 3
    }
}
