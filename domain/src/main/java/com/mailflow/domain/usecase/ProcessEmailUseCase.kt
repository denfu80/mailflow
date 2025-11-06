package com.mailflow.domain.usecase

import com.mailflow.domain.model.AgentContext
import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.MailAgent
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.repository.AgentRepository
import com.mailflow.domain.repository.ContextRepository
import com.mailflow.domain.repository.EmailRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ProcessEmailUseCase @Inject constructor(
    private val agentRepository: AgentRepository,
    private val emailRepository: EmailRepository,
    private val contextRepository: ContextRepository,
    private val geminiService: GeminiService
) {
    suspend operator fun invoke(
        agentId: Long,
        messageId: Long
    ): ProcessingResult<Map<String, Any>> {
        return try {
            val agent = agentRepository.getAgentById(agentId).firstOrNull()
                ?: return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Agent with ID $agentId not found"
                    )
                )

            val messages = emailRepository.getMessagesByAgent(agentId).firstOrNull() ?: emptyList()
            val email = messages.find { it.id == messageId }
                ?: return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Email with ID $messageId not found"
                    )
                )

            if (email.processed) {
                return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Email already processed"
                    )
                )
            }

            val existingContext = contextRepository.getContextByAgent(agentId).firstOrNull() ?: emptyList()

            val prompt = buildPrompt(agent, email, existingContext)

            val aiResponse = geminiService.analyzeEmail(prompt)

            val extractedData = parseAiResponse(aiResponse)

            val contextUpdates = extractedData.map { (key, value) ->
                AgentContext(
                    agentId = agentId,
                    key = key,
                    value = value.toString(),
                    type = inferType(value),
                    updatedAt = System.currentTimeMillis()
                )
            }

            contextRepository.saveContextBatch(contextUpdates)

            emailRepository.markMessageAsProcessed(messageId)

            ProcessingResult.Success(extractedData)
        } catch (e: Exception) {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "Failed to process email: ${e.message}",
                    cause = e
                )
            )
        }
    }

    suspend operator fun invoke(agentId: Long): ProcessingResult<Int> {
        return try {
            val agent = agentRepository.getAgentById(agentId).firstOrNull()
                ?: return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Agent with ID $agentId not found"
                    )
                )

            val unprocessedMessages = emailRepository.getUnprocessedMessagesByAgent(agentId)
                .firstOrNull() ?: emptyList()

            if (unprocessedMessages.isEmpty()) {
                return ProcessingResult.Success(0)
            }

            var successCount = 0
            var errorCount = 0

            unprocessedMessages.forEach { email ->
                val result = invoke(agentId, email.id)
                when (result) {
                    is ProcessingResult.Success -> successCount++
                    is ProcessingResult.Error -> errorCount++
                    else -> {}
                }
            }

            if (errorCount > 0 && successCount == 0) {
                ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.UNKNOWN_ERROR,
                        message = "Failed to process all $errorCount emails"
                    )
                )
            } else {
                ProcessingResult.Success(successCount)
            }
        } catch (e: Exception) {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "Batch processing failed: ${e.message}",
                    cause = e
                )
            )
        }
    }

    private fun buildPrompt(
        agent: MailAgent,
        email: EmailMessage,
        existingContext: List<AgentContext>
    ): String {
        val contextString = existingContext.joinToString("\n") { "${it.key}: ${it.value}" }

        return """
            Agent: ${agent.name}
            Agent Configuration: ${agent.yamlConfig}

            Existing Context:
            $contextString

            New Email:
            From: ${email.sender}
            Subject: ${email.subject}
            Received: ${email.receivedAt}

            Body:
            ${email.body}

            Task: Analyze this email and extract relevant information based on the agent configuration.
            Return the extracted data in a structured format.
        """.trimIndent()
    }

    private fun parseAiResponse(response: String): Map<String, Any> {
        return try {
            mapOf("raw_response" to response)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun inferType(value: Any): String {
        return when (value) {
            is String -> "STRING"
            is Number -> "NUMBER"
            is Boolean -> "BOOLEAN"
            is List<*> -> "LIST"
            is Map<*, *> -> "OBJECT"
            else -> "STRING"
        }
    }
}

interface GeminiService {
    suspend fun analyzeEmail(prompt: String): String
    suspend fun chat(prompt: String, context: Map<String, Any>): String
}
