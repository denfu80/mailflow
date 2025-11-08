package com.mailflow.data.di

import com.mailflow.data.repository.EmailRepositoryImpl
import com.mailflow.data.repository.ProcessingLogRepositoryImpl
import com.mailflow.data.repository.TodoRepositoryImpl
import com.mailflow.data.service.GeminiAIService
import com.mailflow.domain.repository.EmailRepository
import com.mailflow.domain.repository.ProcessingLogRepository
import com.mailflow.domain.repository.TodoRepository
import com.mailflow.domain.service.AIService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindEmailRepository(
        impl: EmailRepositoryImpl
    ): EmailRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(
        impl: TodoRepositoryImpl
    ): TodoRepository

    @Binds
    @Singleton
    abstract fun bindProcessingLogRepository(
        impl: ProcessingLogRepositoryImpl
    ): ProcessingLogRepository

    @Binds
    @Singleton
    abstract fun bindAIService(
        impl: GeminiAIService
    ): AIService
}
