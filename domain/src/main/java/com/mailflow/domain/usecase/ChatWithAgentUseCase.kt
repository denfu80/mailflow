package com.mailflow.domain.usecase

import com.mailflow.domain.model.ChatMessage
import com.mailflow.domain.model.ChatSession
import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.MessageRole
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.repository.AgentRepository
import com.mailflow.domain.repository.ContextRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class ChatWithAgentUseCase @Inject constructor(
    private val agentRepository: AgentRepository,
    private val contextRepository: ContextRepository,
    private val geminiService: GeminiService
) {
    suspend operator fun invoke(
        agentId: Long,
        userMessage: String,
        chatHistory: List<ChatMessage> = emptyList()
    ): ProcessingResult<ChatMessage> {
        return try {
            if (userMessage.isBlank()) {
                return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Message cannot be empty"
                    )
                )
            }

            val agent = agentRepository.getAgentById(agentId).firstOrNull()
                ?: return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Agent with ID $agentId not found"
                    )
                )

            val agentContextList = contextRepository.getContextByAgent(agentId).firstOrNull() ?: emptyList()

            val contextMap = agentContextList.associate { it.key to it.value }

            val prompt = buildChatPrompt(
                agentName = agent.name,
                agentConfig = agent.yamlConfig,
                context = contextMap,
                chatHistory = chatHistory,
                userMessage = userMessage
            )

            val aiResponse = geminiService.chat(prompt, contextMap)

            val responseMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                agentId = agentId,
                role = MessageRole.ASSISTANT,
                content = aiResponse,
                timestamp = System.currentTimeMillis()
            )

            ProcessingResult.Success(responseMessage)
        } catch (e: Exception) {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.API_ERROR,
                    message = "Failed to get response: ${e.message}",
                    cause = e
                )
            )
        }
    }

    fun invokeStreaming(
        agentId: Long,
        userMessage: String,
        chatHistory: List<ChatMessage> = emptyList()
    ): Flow<ProcessingResult<String>> = flow {
        try {
            if (userMessage.isBlank()) {
                emit(
                    ProcessingResult.Error(
                        ProcessingError(
                            type = ErrorType.VALIDATION_ERROR,
                            message = "Message cannot be empty"
                        )
                    )
                )
                return@flow
            }

            val agent = agentRepository.getAgentById(agentId).firstOrNull()
            if (agent == null) {
                emit(
                    ProcessingResult.Error(
                        ProcessingError(
                            type = ErrorType.VALIDATION_ERROR,
                            message = "Agent with ID $agentId not found"
                        )
                    )
                )
                return@flow
            }

            val agentContextList = contextRepository.getContextByAgent(agentId).firstOrNull() ?: emptyList()
            val contextMap = agentContextList.associate { it.key to it.value }

            val prompt = buildChatPrompt(
                agentName = agent.name,
                agentConfig = agent.yamlConfig,
                context = contextMap,
                chatHistory = chatHistory,
                userMessage = userMessage
            )

            emit(ProcessingResult.Loading)

            val response = geminiService.chat(prompt, contextMap)
            emit(ProcessingResult.Success(response))
        } catch (e: Exception) {
            emit(
                ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.API_ERROR,
                        message = "Streaming failed: ${e.message}",
                        cause = e
                    )
                )
            )
        }
    }

    suspend fun getChatSession(agentId: Long): ProcessingResult<ChatSession> {
        return try {
            val agent = agentRepository.getAgentById(agentId).firstOrNull()
                ?: return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Agent with ID $agentId not found"
                    )
                )

            val agentContextList = contextRepository.getContextByAgent(agentId).firstOrNull() ?: emptyList()
            val contextMap = agentContextList.associate { it.key to it.value as Any }

            val session = ChatSession(
                agentId = agentId,
                messages = emptyList(),
                context = contextMap,
                isActive = agent.isActive
            )

            ProcessingResult.Success(session)
        } catch (e: Exception) {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "Failed to load chat session: ${e.message}",
                    cause = e
                )
            )
        }
    }

    private fun buildChatPrompt(
        agentName: String,
        agentConfig: String,
        context: Map<String, String>,
        chatHistory: List<ChatMessage>,
        userMessage: String
    ): String {
        val contextString = context.entries.joinToString("\n") { "${it.key}: ${it.value}" }

        val historyString = chatHistory.takeLast(10).joinToString("\n") { message ->
            when (message.role) {
                MessageRole.USER -> "User: ${message.content}"
                MessageRole.ASSISTANT -> "Assistant: ${message.content}"
                MessageRole.SYSTEM -> "System: ${message.content}"
            }
        }

        return """
            You are an AI assistant for the agent: $agentName

            Agent Configuration:
            $agentConfig

            Current Context (data extracted from emails):
            $contextString

            ${if (historyString.isNotEmpty()) "Chat History:\n$historyString\n" else ""}

            User: $userMessage

            Please provide a helpful response based on the agent's context and configuration.
        """.trimIndent()
    }
}
