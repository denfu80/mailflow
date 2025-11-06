package com.mailflow.domain.model

data class AgentConfiguration(
    val filters: EmailFilters,
    val geminiPrompt: String,
    val contextSchema: Map<String, ContextFieldType>
)

data class EmailFilters(
    val senders: List<String> = emptyList(),
    val subjects: List<String> = emptyList(),
    val hasAttachments: Boolean? = null
) {
    fun matches(email: EmailMessage): Boolean {
        val senderMatches = senders.isEmpty() || senders.any { pattern ->
            email.sender.contains(pattern, ignoreCase = true)
        }

        val subjectMatches = subjects.isEmpty() || subjects.any { pattern ->
            email.subject.contains(pattern, ignoreCase = true)
        }

        val attachmentMatches = hasAttachments == null || hasAttachments == false

        return senderMatches && subjectMatches && attachmentMatches
    }
}

enum class ContextFieldType {
    STRING,
    NUMBER,
    BOOLEAN,
    DATE,
    LIST,
    OBJECT
}
