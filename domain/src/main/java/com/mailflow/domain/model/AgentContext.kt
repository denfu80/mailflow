package com.mailflow.domain.model

data class AgentContext(
    val id: Long = 0,
    val agentId: Long,
    val key: String,
    val value: String,
    val type: String,
    val updatedAt: Long = System.currentTimeMillis()
)
