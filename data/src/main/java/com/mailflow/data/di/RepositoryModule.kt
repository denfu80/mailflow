package com.mailflow.data.di

import com.mailflow.data.repository.AgentRepositoryImpl
import com.mailflow.data.repository.ContextRepositoryImpl
import com.mailflow.data.repository.EmailRepositoryImpl
import com.mailflow.domain.repository.AgentRepository
import com.mailflow.domain.repository.ContextRepository
import com.mailflow.domain.repository.EmailRepository
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
    abstract fun bindAgentRepository(
        impl: AgentRepositoryImpl
    ): AgentRepository

    @Binds
    @Singleton
    abstract fun bindEmailRepository(
        impl: EmailRepositoryImpl
    ): EmailRepository

    @Binds
    @Singleton
    abstract fun bindContextRepository(
        impl: ContextRepositoryImpl
    ): ContextRepository
}
