package com.mailflow.domain.usecase

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
    // private val geminiService: GeminiService // Will be added later
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

            // val aiResponse = geminiService.analyzeEmail(prompt)
            // Placeholder for AI response
            val aiResponse = "Extracted Todo: Pay the electricity bill by Friday."

            val extractedTodo = parseAiResponse(aiResponse)

            if (extractedTodo.isBlank()) {
                return ProcessingResult.Success("No todo found.")
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
            From the following email content, extract a single, concise to-do item.
            The to-do item should be a clear action.
            If no specific task is found, return an empty string.

            Email Subject: ${email.subject}
            Email Body:
            ${email.body}

            Extracted Todo:
        """.trimIndent()
    }

    private fun parseAiResponse(response: String): String {
        // Simple parsing for now, assuming the AI returns the todo directly.
        return response.removePrefix("Extracted Todo:").trim()
    }
}

