package com.mailflow.domain.service

interface AIService {
    suspend fun generateContent(prompt: String): Result<String>
}
