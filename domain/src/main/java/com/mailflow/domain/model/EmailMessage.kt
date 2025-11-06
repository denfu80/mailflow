package com.mailflow.domain.model

data class EmailMessage(
    val id: Long = 0,
    val agentId: Long,
    val messageId: String,
    val subject: String,
    val sender: String,
    val receivedAt: Long,
    val body: String,
    val processed: Boolean = false,
    val processedAt: Long? = null
)
