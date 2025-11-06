package com.mailflow.data.di

import com.mailflow.data.remote.gmail.GmailClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GmailModule {

    @Provides
    @Singleton
    fun provideGmailClient(): GmailClient {
        return GmailClient()
    }
}
