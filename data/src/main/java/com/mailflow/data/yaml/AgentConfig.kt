package com.mailflow.data.yaml

data class AgentConfig(
    val name: String,
    val description: String? = null,
    val filters: EmailFilters? = null,
    val geminiPrompt: String,
    val contextSchema: Map<String, String> = emptyMap(),
    val actions: List<AgentAction>? = null
)

data class EmailFilters(
    val senders: List<String>? = null,
    val subjects: List<String>? = null,
    val hasAttachments: Boolean? = null,
    val keywords: List<String>? = null
)

data class AgentAction(
    val type: String,
    val config: Map<String, Any>? = null
)
