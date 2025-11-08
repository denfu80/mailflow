package com.mailflow.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mailflow.data.notification.NotificationManager
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.repository.EmailRepository
import com.mailflow.domain.usecase.ProcessEmailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class EmailProcessingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val processEmailUseCase: ProcessEmailUseCase,
    private val emailRepository: EmailRepository,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val unprocessedMessages = emailRepository.getUnprocessedMessages().first()
            var successCount = 0

            for (message in unprocessedMessages) {
                // TODO: Get list name from settings
                val result = processEmailUseCase(message, "inbox-test")

                if (result is ProcessingResult.Success) {
                    successCount++
                    notificationManager.showTodoCreatedNotification(
                        todoTitle = result.data,
                        listName = "inbox-test"
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        const val WORK_NAME = "email_processing_work"
        const val MAX_RETRY_ATTEMPTS = 3
    }
}

