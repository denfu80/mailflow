package com.mailflow.domain.model

data class ChatMessage(
    val id: String,
    val agentId: Long,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatSession(
    val agentId: Long,
    val messages: List<ChatMessage>,
    val context: Map<String, Any>,
    val isActive: Boolean = true
)
