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
    private val agentRepository: AgentRepository
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
            val aiResponse = ChatMessageUiModel(
                id = UUID.randomUUID().toString(),
                content = "This is a placeholder AI response. The actual Gemini integration will be implemented in Phase 4.",
                isFromUser = false
            )

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiResponse,
                isSending = false
            )
        }
    }
}
