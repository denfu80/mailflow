package com.mailflow.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mailflow.data.notification.NotificationManager
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.usecase.ProcessEmailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EmailProcessingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val processEmailUseCase: ProcessEmailUseCase,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val agentId = inputData.getLong(KEY_AGENT_ID, -1L)

            if (agentId == -1L) {
                return Result.failure(
                    workDataOf(KEY_ERROR_MESSAGE to "Agent ID is required")
                )
            }

            val result = processEmailUseCase(agentId)

            when (result) {
                is ProcessingResult.Success -> {
                    val processedCount = result.data

                    notificationManager.cancelProcessingNotification()

                    if (processedCount > 0) {
                        notificationManager.showProcessingCompleteNotification(
                            agentName = "Agent",
                            processedCount = processedCount
                        )
                    }

                    val outputData = workDataOf(
                        KEY_PROCESSED_COUNT to processedCount,
                        KEY_SUCCESS to true
                    )
                    Result.success(outputData)
                }

                is ProcessingResult.Error -> {
                    notificationManager.cancelProcessingNotification()

                    val outputData = workDataOf(
                        KEY_ERROR_MESSAGE to result.error.message,
                        KEY_SUCCESS to false
                    )

                    if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                        Result.failure(outputData)
                    } else {
                        Result.retry()
                    }
                }

                is ProcessingResult.Loading -> {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                Result.failure(
                    workDataOf(
                        KEY_ERROR_MESSAGE to "Failed after $MAX_RETRY_ATTEMPTS attempts: ${e.message}",
                        KEY_SUCCESS to false
                    )
                )
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        const val WORK_NAME = "email_processing_work"
        const val KEY_AGENT_ID = "agent_id"
        const val KEY_PROCESSED_COUNT = "processed_count"
        const val KEY_SUCCESS = "success"
        const val KEY_ERROR_MESSAGE = "error_message"
        const val MAX_RETRY_ATTEMPTS = 3
    }
}
