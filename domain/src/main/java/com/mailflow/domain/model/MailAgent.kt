package com.mailflow.domain.model

data class MailAgent(
    val id: Long = 0,
    val name: String,
    val description: String,
    val yamlConfig: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
