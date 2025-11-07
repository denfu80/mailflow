package com.mailflow.data.di

import com.mailflow.core.util.RateLimiter
import com.mailflow.data.BuildConfig
import com.mailflow.data.remote.gemini.GeminiClient
import com.mailflow.data.remote.gemini.GeminiServiceImpl
import com.mailflow.data.remote.gmail.GmailServiceImpl
import com.mailflow.domain.usecase.GeminiService
import com.mailflow.domain.usecase.GmailService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideGeminiApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    @Provides
    @Singleton
    fun provideGeminiRateLimiter(): RateLimiter {
        return RateLimiter.perMinute(10)
    }

    @Provides
    @Singleton
    fun provideGeminiClient(
        apiKey: String,
        rateLimiter: RateLimiter
    ): GeminiClient {
        return GeminiClient(apiKey, rateLimiter)
    }

    @Provides
    @Singleton
    fun provideGeminiService(geminiClient: GeminiClient): GeminiService {
        return GeminiServiceImpl(geminiClient)
    }

    @Provides
    @Singleton
    fun provideGmailService(gmailServiceImpl: GmailServiceImpl): GmailService {
        return gmailServiceImpl
    }
}
