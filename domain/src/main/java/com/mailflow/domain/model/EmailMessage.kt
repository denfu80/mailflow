package com.mailflow.domain.model

data class EmailMessage(
    val id: Long = 0,
    val messageId: String,
    val subject: String,
    val sender: String,
    val receivedAt: Long,
    val body: String,
    val processed: Boolean = false,
    val processedAt: Long? = null,
    val selected: Boolean = false,
    val extractedTodos: String? = null,
    val todosSynced: Boolean = false
)
