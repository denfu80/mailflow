package com.mailflow.data.di

import com.mailflow.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun provideGeminiApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }
}
