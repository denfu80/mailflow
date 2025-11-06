package com.mailflow.data.di

import com.mailflow.core.di.GeminiApiKey
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
    fun provideGeminiClient(@GeminiApiKey apiKey: String): GeminiClient {
        return GeminiClient(apiKey)
    }
}
