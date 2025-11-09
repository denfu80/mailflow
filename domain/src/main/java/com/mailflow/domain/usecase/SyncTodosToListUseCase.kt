package com.mailflow.domain.usecase

import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.repository.EmailRepository
import com.mailflow.domain.repository.TodoRepository
import javax.inject.Inject

/**
 * Syncs extracted TODOs from emails to the configured TODO list.
 */
class SyncTodosToListUseCase @Inject constructor(
    private val emailRepository: EmailRepository,
    private val todoRepository: TodoRepository
) {
    /**
     * Syncs TODOs from a single email to the list.
     */
    suspend fun syncSingle(message: EmailMessage, todoListName: String): ProcessingResult<String> {
        return try {
            if (message.extractedTodos.isNullOrBlank()) {
                return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "No TODOs to sync"
                    )
                )
            }

            if (message.todosSynced) {
                return ProcessingResult.Success("TODOs already synced")
            }

            val result = todoRepository.addTodo(
                listName = todoListName,
                todoText = message.extractedTodos
            )

            if (result.isSuccess) {
                emailRepository.markTodosAsSynced(message.id)
                ProcessingResult.Success("TODO synced: ${message.extractedTodos}")
            } else {
                ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.API_ERROR,
                        message = "Failed to sync TODO: ${result.exceptionOrNull()?.message}",
                        cause = result.exceptionOrNull()
                    )
                )
            }
        } catch (e: Exception) {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "Failed to sync TODO: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Syncs TODOs from multiple emails to the list.
     */
    suspend fun syncMultiple(messages: List<EmailMessage>, todoListName: String): ProcessingResult<BatchSyncResult> {
        val results = mutableListOf<TodoSyncResult>()
        var successCount = 0
        var failureCount = 0
        var skippedCount = 0

        for (message in messages) {
            if (message.extractedTodos.isNullOrBlank()) {
                skippedCount++
                results.add(TodoSyncResult(message.id, message.subject, false, "No TODOs", true))
                continue
            }

            if (message.todosSynced) {
                skippedCount++
                results.add(TodoSyncResult(message.id, message.subject, false, "Already synced", true))
                continue
            }

            val result = syncSingle(message, todoListName)
            when (result) {
                is ProcessingResult.Success -> {
                    successCount++
                    results.add(TodoSyncResult(message.id, message.subject, true, result.data, false))
                }
                is ProcessingResult.Error -> {
                    failureCount++
                    results.add(TodoSyncResult(message.id, message.subject, false, result.error.message, false))
                }
                is ProcessingResult.Loading -> {} // Should not happen
            }
        }

        return ProcessingResult.Success(
            BatchSyncResult(
                total = messages.size,
                successful = successCount,
                failed = failureCount,
                skipped = skippedCount,
                results = results
            )
        )
    }
}

data class BatchSyncResult(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val skipped: Int,
    val results: List<TodoSyncResult>
)

data class TodoSyncResult(
    val emailId: Long,
    val subject: String,
    val success: Boolean,
    val message: String,
    val skipped: Boolean
)
