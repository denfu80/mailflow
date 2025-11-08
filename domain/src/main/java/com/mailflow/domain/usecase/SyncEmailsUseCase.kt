package com.mailflow.domain.usecase

import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.SyncResult
import com.mailflow.domain.repository.EmailRepository
import javax.inject.Inject

class SyncEmailsUseCase @Inject constructor(
    private val emailRepository: EmailRepository,
) {
    suspend operator fun invoke(): SyncResult {
        return try {
            val fetchedMessages = emailRepository.fetchNewMessages()

            if (fetchedMessages.isEmpty()) {
                return SyncResult.Success(
                    messagesFetched = 0,
                    messagesProcessed = 0,
                    errors = 0
                )
            }

            val saveResult = emailRepository.saveMessages(fetchedMessages)

            if (saveResult.isSuccess) {
                SyncResult.Success(
                    messagesFetched = fetchedMessages.size,
                    messagesProcessed = fetchedMessages.size,
                    errors = 0
                )
            } else {
                SyncResult.Failure(
                    ProcessingError(
                        type = ErrorType.DATABASE_ERROR,
                        message = "Failed to save messages: ${saveResult.exceptionOrNull()?.message}",
                        cause = saveResult.exceptionOrNull()
                    )
                )
            }
        } catch (e: Exception) {
            SyncResult.Failure(
                ProcessingError(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "Sync failed: ${e.message}",
                    cause = e
                )
            )
        }
    }
}

