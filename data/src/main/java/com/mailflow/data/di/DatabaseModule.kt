package com.mailflow.data.di

import android.content.Context
import androidx.room.Room
import com.mailflow.data.database.MailFlowDatabase
import com.mailflow.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MailFlowDatabase {
        return Room.databaseBuilder(
            context,
            MailFlowDatabase::class.java,
            MailFlowDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMailAgentDao(database: MailFlowDatabase): MailAgentDao {
        return database.mailAgentDao()
    }

    @Provides
    fun provideEmailMessageDao(database: MailFlowDatabase): EmailMessageDao {
        return database.emailMessageDao()
    }

    @Provides
    fun provideAgentContextDao(database: MailFlowDatabase): AgentContextDao {
        return database.agentContextDao()
    }

    @Provides
    fun provideProcessingJobDao(database: MailFlowDatabase): ProcessingJobDao {
        return database.processingJobDao()
    }
}
