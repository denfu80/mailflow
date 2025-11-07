package com.mailflow.data.remote.gemini

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.mailflow.core.util.RateLimiter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GeminiClient @Inject constructor(
    private val apiKey: String,
    private val rateLimiter: RateLimiter
) {
    companion object {
        private const val TAG = "GeminiClient"
        private const val MIN_DELAY_MS = 2000L
        private const val MAX_DELAY_MS = 3000L
    }
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash-exp",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 2048
            }
        )
    }

    suspend fun generateContent(prompt: String): Result<String> {
        return try {
            rateLimiter.acquire()
            Log.d(TAG, "Acquired rate limit token. Remaining: ${rateLimiter.getRemainingRequests()}")

            val response = model.generateContent(prompt)

            val delayMs = Random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS)
            Log.d(TAG, "Request completed. Delaying for ${delayMs}ms")
            delay(delayMs)

            Result.success(response.text ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun generateContentWithContext(
        systemPrompt: String,
        userPrompt: String,
        context: Map<String, Any> = emptyMap()
    ): Result<String> {
        return try {
            rateLimiter.acquire()
            Log.d(TAG, "Acquired rate limit token for context generation")

            val fullPrompt = buildPromptWithContext(systemPrompt, userPrompt, context)
            val response = model.generateContent(fullPrompt)

            val delayMs = Random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS)
            delay(delayMs)

            Result.success(response.text ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content with context: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun analyzeEmail(
        emailSubject: String,
        emailBody: String,
        agentPrompt: String,
        contextSchema: Map<String, String>
    ): Result<String> {
        return try {
            rateLimiter.acquire()
            Log.d(TAG, "Acquired rate limit token for email analysis. Subject: $emailSubject")

            val prompt = buildEmailAnalysisPrompt(
                emailSubject = emailSubject,
                emailBody = emailBody,
                agentPrompt = agentPrompt,
                contextSchema = contextSchema
            )
            val response = model.generateContent(prompt)

            val delayMs = Random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS)
            Log.d(TAG, "Email analysis completed. Delaying for ${delayMs}ms")
            delay(delayMs)

            Result.success(response.text ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing email: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun streamContent(prompt: String): Flow<String> {
        return model.generateContentStream(prompt)
            .map { response -> response.text ?: "" }
    }

    suspend fun chatWithAgent(
        agentContext: String,
        userMessage: String,
        chatHistory: List<ChatMessage> = emptyList()
    ): Result<String> {
        return try {
            rateLimiter.acquire()
            Log.d(TAG, "Acquired rate limit token for chat")

            val chat = model.startChat(
                history = chatHistory.map { message ->
                    content(message.role) {
                        text(message.content)
                    }
                }
            )

            val fullMessage = if (agentContext.isNotEmpty()) {
                "Context: $agentContext\n\nUser: $userMessage"
            } else {
                userMessage
            }

            val response = chat.sendMessage(fullMessage)

            val delayMs = Random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS)
            delay(delayMs)

            Result.success(response.text ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error in chat: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun buildPromptWithContext(
        systemPrompt: String,
        userPrompt: String,
        context: Map<String, Any>
    ): String {
        val contextString = context.entries.joinToString("\n") { (key, value) ->
            "- $key: $value"
        }

        return """
            |System Instructions:
            |$systemPrompt
            |
            |Context:
            |$contextString
            |
            |User Request:
            |$userPrompt
        """.trimMargin()
    }

    private fun buildEmailAnalysisPrompt(
        emailSubject: String,
        emailBody: String,
        agentPrompt: String,
        contextSchema: Map<String, String>
    ): String {
        val schemaDescription = contextSchema.entries.joinToString("\n") { (key, type) ->
            "- $key ($type)"
        }

        return """
            |You are an email analysis assistant. Analyze the following email and extract structured information.
            |
            |Agent Instructions:
            |$agentPrompt
            |
            |Expected Output Schema:
            |$schemaDescription
            |
            |Email to Analyze:
            |Subject: $emailSubject
            |
            |Body:
            |$emailBody
            |
            |Please provide the extracted information in JSON format matching the schema above.
            |Only output the JSON, no additional text.
        """.trimMargin()
    }

    data class ChatMessage(
        val role: String,
        val content: String
    )
}
