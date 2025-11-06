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
            val agentId = inputData.getLong(KEY_AGENT_ID, -1L)

            val syncResult = if (agentId != -1L) {
                syncEmailsUseCase(agentId)
            } else {
                syncEmailsUseCase()
            }

            when (syncResult) {
                is SyncResult.Success -> {
                    if (syncResult.messagesProcessed > 0) {
                        notificationManager.showSyncNotification(
                            messagesFetched = syncResult.messagesFetched,
                            messagesProcessed = syncResult.messagesProcessed
                        )
                    }

                    val outputData = workDataOf(
                        KEY_MESSAGES_FETCHED to syncResult.messagesFetched,
                        KEY_MESSAGES_PROCESSED to syncResult.messagesProcessed,
                        KEY_ERRORS to syncResult.errors
                    )
                    Result.success(outputData)
                }

                is SyncResult.PartialSuccess -> {
                    if (syncResult.messagesProcessed > 0) {
                        notificationManager.showSyncNotification(
                            messagesFetched = syncResult.messagesFetched,
                            messagesProcessed = syncResult.messagesProcessed
                        )
                    }

                    val outputData = workDataOf(
                        KEY_MESSAGES_FETCHED to syncResult.messagesFetched,
                        KEY_MESSAGES_PROCESSED to syncResult.messagesProcessed,
                        KEY_ERRORS to syncResult.errors,
                        KEY_ERROR_MESSAGES to syncResult.errorMessages.joinToString(", ")
                    )
                    Result.success(outputData)
                }

                is SyncResult.Failure -> {
                    if (runAttemptCount >= MAX_RETRY_ATTEMPTS - 1) {
                        notificationManager.showSyncFailedNotification(syncResult.error.message)
                    }

                    val outputData = workDataOf(
                        KEY_ERROR_MESSAGE to syncResult.error.message
                    )
                    Result.retry()
                }
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
        const val KEY_AGENT_ID = "agent_id"
        const val KEY_MESSAGES_FETCHED = "messages_fetched"
        const val KEY_MESSAGES_PROCESSED = "messages_processed"
        const val KEY_ERRORS = "errors"
        const val KEY_ERROR_MESSAGES = "error_messages"
        const val KEY_ERROR_MESSAGE = "error_message"
        const val MAX_RETRY_ATTEMPTS = 3
    }
}
