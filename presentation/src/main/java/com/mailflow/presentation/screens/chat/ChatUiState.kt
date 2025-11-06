package com.mailflow.presentation.screens.chat

data class ChatUiState(
    val agentName: String = "",
    val messages: List<ChatMessageUiModel> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

data class ChatMessageUiModel(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
