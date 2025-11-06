package com.mailflow.presentation.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mailflow.domain.repository.AgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val agentRepository: AgentRepository,
    private val contextRepository: com.mailflow.domain.repository.ContextRepository,
    private val chatWithAgentUseCase: com.mailflow.domain.usecase.ChatWithAgentUseCase
) : ViewModel() {

    private val agentId: String = checkNotNull(savedStateHandle["agentId"])

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadAgent()
    }

    private fun loadAgent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            agentRepository.getAgentById(agentId.toLongOrNull() ?: 0L)
                .collect { agent ->
                    _uiState.value = _uiState.value.copy(
                        agentName = agent?.name ?: "Unknown Agent",
                        isLoading = false
                    )
                }
        }
    }

    fun onMessageChanged(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }

    fun sendMessage() {
        val message = _uiState.value.currentMessage.trim()
        if (message.isEmpty() || _uiState.value.isSending) return

        val userMessage = ChatMessageUiModel(
            id = UUID.randomUUID().toString(),
            content = message,
            isFromUser = true
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            currentMessage = "",
            isSending = true
        )

        viewModelScope.launch {
            try {
                val agentIdLong = agentId.toLongOrNull() ?: 0L

                val chatHistory = _uiState.value.messages.map { msg ->
                    com.mailflow.domain.model.ChatMessage(
                        id = msg.id,
                        agentId = agentIdLong,
                        role = if (msg.isFromUser)
                            com.mailflow.domain.model.MessageRole.USER
                        else
                            com.mailflow.domain.model.MessageRole.ASSISTANT,
                        content = msg.content,
                        timestamp = msg.timestamp
                    )
                }

                val result = chatWithAgentUseCase(
                    agentId = agentIdLong,
                    userMessage = message,
                    chatHistory = chatHistory
                )

                when (result) {
                    is com.mailflow.domain.model.ProcessingResult.Success -> {
                        val aiResponse = ChatMessageUiModel(
                            id = result.data.id,
                            content = result.data.content,
                            isFromUser = false,
                            timestamp = result.data.timestamp
                        )

                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + aiResponse,
                            isSending = false,
                            error = null
                        )
                    }
                    is com.mailflow.domain.model.ProcessingResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            error = result.error.message
                        )
                    }
                    is com.mailflow.domain.model.ProcessingResult.Loading -> {
                        // Keep sending state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = "Failed to send message: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
