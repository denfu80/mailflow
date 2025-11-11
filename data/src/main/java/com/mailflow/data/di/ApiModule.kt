package com.mailflow.data.di

import com.mailflow.data.BuildConfig
import com.mailflow.data.remote.api.TodoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun provideGeminiApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    @Provides
    fun provideTodoApiService(retrofit: Retrofit): TodoApiService {
        return retrofit.create(TodoApiService::class.java)
    }
}