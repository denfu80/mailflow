package com.mailflow.domain.model

data class ProcessingLog(
    val id: Long = 0,
    val status: String,
    val startTime: Long,
    val endTime: Long? = null,
    val error: String? = null,
    val processedCount: Int = 0
) {
    fun toDisplayString(): String {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(startTime))

        return when {
            error != null -> "❌ $timeStr - Failed: $error"
            endTime != null -> "✅ $timeStr - Processed $processedCount emails"
            else -> "⏳ $timeStr - Processing..."
        }
    }
}
