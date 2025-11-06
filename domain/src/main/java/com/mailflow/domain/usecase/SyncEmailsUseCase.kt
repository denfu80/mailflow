package com.mailflow.domain.usecase

import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.MailAgent
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.SyncResult
import com.mailflow.domain.repository.AgentRepository
import com.mailflow.domain.repository.EmailRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SyncEmailsUseCase @Inject constructor(
    private val agentRepository: AgentRepository,
    private val emailRepository: EmailRepository,
    private val gmailService: GmailService
) {
    suspend operator fun invoke(agentId: Long): SyncResult {
        return try {
            val agent = agentRepository.getAgentById(agentId).firstOrNull()
                ?: return SyncResult.Failure(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Agent with ID $agentId not found"
                    )
                )

            if (!agent.isActive) {
                return SyncResult.Failure(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Agent '${agent.name}' is not active"
                    )
                )
            }

            val fetchedMessages = gmailService.fetchNewMessages()

            val filteredMessages = filterMessagesByAgent(fetchedMessages, agent)

            if (filteredMessages.isEmpty()) {
                return SyncResult.Success(
                    messagesFetched = fetchedMessages.size,
                    messagesProcessed = 0,
                    errors = 0
                )
            }

            val saveResult = emailRepository.saveMessages(filteredMessages)

            when {
                saveResult.isSuccess -> {
                    return SyncResult.Success(
                        messagesFetched = fetchedMessages.size,
                        messagesProcessed = filteredMessages.size,
                        errors = 0
                    )
                }
                saveResult.isFailure -> {
                    return SyncResult.Failure(
                        ProcessingError(
                            type = ErrorType.DATABASE_ERROR,
                            message = "Failed to save messages: ${saveResult.exceptionOrNull()?.message}",
                            cause = saveResult.exceptionOrNull()
                        )
                    )
                }
                else -> {
                    return SyncResult.Failure(
                        ProcessingError(
                            type = ErrorType.UNKNOWN_ERROR,
                            message = "Unknown error during sync"
                        )
                    )
                }
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

    suspend operator fun invoke(): SyncResult {
        return try {
            val activeAgents = agentRepository.getActiveAgents().firstOrNull() ?: emptyList()

            if (activeAgents.isEmpty()) {
                return SyncResult.Success(
                    messagesFetched = 0,
                    messagesProcessed = 0,
                    errors = 0
                )
            }

            val fetchedMessages = gmailService.fetchNewMessages()
            var totalProcessed = 0
            var totalErrors = 0
            val errorMessages = mutableListOf<String>()

            activeAgents.forEach { agent ->
                val filteredMessages = filterMessagesByAgent(fetchedMessages, agent)

                if (filteredMessages.isNotEmpty()) {
                    val saveResult = emailRepository.saveMessages(filteredMessages)

                    when {
                        saveResult.isSuccess -> {
                            totalProcessed += filteredMessages.size
                        }
                        saveResult.isFailure -> {
                            totalErrors++
                            errorMessages.add("Agent '${agent.name}': ${saveResult.exceptionOrNull()?.message}")
                        }
                    }
                }
            }

            when {
                totalErrors == 0 -> SyncResult.Success(
                    messagesFetched = fetchedMessages.size,
                    messagesProcessed = totalProcessed,
                    errors = 0
                )
                totalProcessed > 0 -> SyncResult.PartialSuccess(
                    messagesFetched = fetchedMessages.size,
                    messagesProcessed = totalProcessed,
                    errors = totalErrors,
                    errorMessages = errorMessages
                )
                else -> SyncResult.Failure(
                    ProcessingError(
                        type = ErrorType.DATABASE_ERROR,
                        message = "All agents failed to sync: ${errorMessages.joinToString(", ")}"
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

    private fun filterMessagesByAgent(
        messages: List<EmailMessage>,
        agent: MailAgent
    ): List<EmailMessage> {
        return messages.filter { message ->
            message.agentId == agent.id
        }
    }
}

interface GmailService {
    suspend fun fetchNewMessages(): List<EmailMessage>
}
