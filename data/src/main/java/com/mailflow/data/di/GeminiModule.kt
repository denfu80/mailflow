package com.mailflow.data.di

import com.mailflow.core.di.GeminiApiKey
import com.mailflow.core.util.RateLimiter
import com.mailflow.data.remote.gemini.GeminiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {

    @Provides
    @Singleton
    @GeminiApiKey
    fun provideGeminiApiKey(): String {
        return com.mailflow.app.BuildConfig.GEMINI_API_KEY
    }

    @Provides
    @Singleton
    fun provideGeminiRateLimiter(): RateLimiter {
        // Gemini free tier: 15 requests per minute
        return RateLimiter.perMinute(15)
    }

    @Provides
    @Singleton
    fun provideGeminiClient(
        @GeminiApiKey apiKey: String,
        rateLimiter: RateLimiter
    ): GeminiClient {
        return GeminiClient(apiKey, rateLimiter)
    }
}
