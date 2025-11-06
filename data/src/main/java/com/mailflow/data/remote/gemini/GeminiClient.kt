package com.mailflow.data.remote.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiClient @Inject constructor(
    private val apiKey: String
) {
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
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
            val response = model.generateContent(prompt)
            Result.success(response.text ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateContentWithContext(
        systemPrompt: String,
        userPrompt: String,
        context: Map<String, Any> = emptyMap()
    ): Result<String> {
        return try {
            val fullPrompt = buildPromptWithContext(systemPrompt, userPrompt, context)
            val response = model.generateContent(fullPrompt)
            Result.success(response.text ?: "")
        } catch (e: Exception) {
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
            val prompt = buildEmailAnalysisPrompt(
                emailSubject = emailSubject,
                emailBody = emailBody,
                agentPrompt = agentPrompt,
                contextSchema = contextSchema
            )
            val response = model.generateContent(prompt)
            Result.success(response.text ?: "")
        } catch (e: Exception) {
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
            Result.success(response.text ?: "")
        } catch (e: Exception) {
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
