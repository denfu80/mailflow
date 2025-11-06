package com.mailflow.data.remote.gemini

import com.mailflow.domain.usecase.GeminiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiServiceImpl @Inject constructor(
    private val geminiClient: GeminiClient
) : GeminiService {

    override suspend fun analyzeEmail(prompt: String): String {
        val result = geminiClient.generateContent(prompt)
        return result.getOrThrow()
    }

    override suspend fun chat(prompt: String, context: Map<String, Any>): String {
        val contextString = context.entries.joinToString("\n") { (key, value) ->
            "$key: $value"
        }

        val fullPrompt = if (context.isNotEmpty()) {
            """
            Context:
            $contextString

            User Message:
            $prompt
            """.trimIndent()
        } else {
            prompt
        }

        val result = geminiClient.generateContent(fullPrompt)
        return result.getOrThrow()
    }
}
