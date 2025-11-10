package com.mailflow.domain.usecase

import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.repository.EmailRepository
import com.mailflow.domain.service.AIService
import javax.inject.Inject

/**
 * Analyzes emails and extracts TODOs, storing them locally in the email entity.
 * Does NOT automatically sync TODOs to external list - use SyncTodosToListUseCase for that.
 */
class AnalyzeEmailsUseCase @Inject constructor(
    private val emailRepository: EmailRepository,
    private val aiService: AIService
) {
    /**
     * Analyzes a single email and extracts TODOs.
     */
    suspend fun analyzeSingle(message: EmailMessage): ProcessingResult<String> {
        return try {
            if (message.processed) {
                return ProcessingResult.Success("Email already analyzed")
            }

            val prompt = buildTodoExtractionPrompt(message)
            val aiResult = aiService.generateContent(prompt)

            if (aiResult.isFailure) {
                return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.API_ERROR,
                        message = "AI analysis failed: ${aiResult.exceptionOrNull()?.message}",
                        cause = aiResult.exceptionOrNull()
                    )
                )
            }

            val aiResponse = aiResult.getOrNull() ?: ""
            val extractedTodo = parseAiResponse(aiResponse)

            // Store extracted TODO in email entity (even if empty)
            emailRepository.updateEmailWithTodos(
                messageId = message.id,
                todos = extractedTodo.takeIf { it.isNotBlank() }
            )

            ProcessingResult.Success(
                if (extractedTodo.isBlank()) "No TODO found" else "TODO extracted: $extractedTodo"
            )
        } catch (e: Exception) {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "Failed to analyze email: ${e.message}",
                    cause = e
                )
            )
        }
    }

    /**
     * Analyzes multiple emails in batch.
     */
    suspend fun analyzeMultiple(messages: List<EmailMessage>): ProcessingResult<BatchAnalysisResult> {
        val results = mutableListOf<EmailAnalysisResult>()
        var successCount = 0
        var failureCount = 0

        for (message in messages) {
            val result = analyzeSingle(message)
            when (result) {
                is ProcessingResult.Success -> {
                    successCount++
                    results.add(EmailAnalysisResult(message.id, message.subject, true, result.data))
                }
                is ProcessingResult.Error -> {
                    failureCount++
                    results.add(EmailAnalysisResult(message.id, message.subject, false, result.error.message))
                }
                is ProcessingResult.Loading -> {} // Should not happen
            }
        }

        return ProcessingResult.Success(
            BatchAnalysisResult(
                total = messages.size,
                successful = successCount,
                failed = failureCount,
                results = results
            )
        )
    }

    private fun buildTodoExtractionPrompt(email: EmailMessage): String {
        return """
            You are a helpful assistant that extracts actionable tasks from emails.

            Analyze the following email and extract ONE clear, concise to-do item.

            Rules:
            - Extract only ONE actionable task (the most important one)
            - The task should be written as a clear action item
            - Keep it concise (max 100 characters)
            - If there is no actionable task in the email, respond with: "NONE"
            - Do not include explanations, only output the task text or "NONE"

            Email Subject: ${email.subject}

            Email Body:
            ${email.body}

            Extracted Task:
        """.trimIndent()
    }

    private fun parseAiResponse(response: String): String {
        val cleaned = response.trim()

        // Check if AI explicitly said there's no task
        if (cleaned.equals("NONE", ignoreCase = true)) {
            return ""
        }

        // Remove common prefixes that AI might add
        val prefixes = listOf(
            "Extracted Task:",
            "Task:",
            "To-do:",
            "Action:"
        )

        var result = cleaned
        for (prefix in prefixes) {
            if (result.startsWith(prefix, ignoreCase = true)) {
                result = result.substring(prefix.length).trim()
            }
        }

        // Limit length to 200 characters
        return if (result.length > 200) {
            result.substring(0, 197) + "..."
        } else {
            result
        }
    }
}

data class BatchAnalysisResult(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val results: List<EmailAnalysisResult>
)

data class EmailAnalysisResult(
    val emailId: Long,
    val subject: String,
    val success: Boolean,
    val message: String
)
