package com.mailflow.data.service

import com.mailflow.data.remote.gemini.GeminiClient
import com.mailflow.domain.service.AIService
import javax.inject.Inject

class GeminiAIService @Inject constructor(
    private val geminiClient: GeminiClient
) : AIService {
    override suspend fun generateContent(prompt: String): Result<String> {
        return geminiClient.generateContent(prompt)
    }
}
