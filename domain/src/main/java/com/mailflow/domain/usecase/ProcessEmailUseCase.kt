package com.mailflow.domain.usecase

import com.mailflow.data.remote.gemini.GeminiClient
import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.repository.EmailRepository
import com.mailflow.domain.repository.TodoRepository
import javax.inject.Inject

class ProcessEmailUseCase @Inject constructor(
    private val emailRepository: EmailRepository,
    private val todoRepository: TodoRepository,
    private val geminiClient: GeminiClient
) {
    suspend operator fun invoke(
        message: EmailMessage,
        todoListName: String
    ): ProcessingResult<String> {
        return try {
            if (message.processed) {
                return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "Email already processed"
                    )
                )
            }

            val prompt = buildTodoExtractionPrompt(message)

            // Use Gemini to extract todo from email
            val aiResult = geminiClient.generateContent(prompt)

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

            if (extractedTodo.isBlank()) {
                // Mark as processed even if no todo found
                emailRepository.markMessageAsProcessed(message.id)
                return ProcessingResult.Success("No todo found in email.")
            }

            val result = todoRepository.addTodo(
                listName = todoListName,
                todoText = extractedTodo
            )

            if (result.isSuccess) {
                emailRepository.markMessageAsProcessed(message.id)
                ProcessingResult.Success(extractedTodo)
            } else {
                ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.API_ERROR,
                        message = "Failed to create todo: ${result.exceptionOrNull()?.message}",
                        cause = result.exceptionOrNull()
                    )
                )
            }
        } catch (e: Exception) {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "Failed to process email: ${e.message}",
                    cause = e
                )
            )
        }
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

