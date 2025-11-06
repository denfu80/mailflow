package com.mailflow.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mailflow.data.database.dao.*
import com.mailflow.data.model.*

@Database(
    entities = [
        MailAgentEntity::class,
        EmailMessageEntity::class,
        AgentContextEntity::class,
        ProcessingJobEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MailFlowDatabase : RoomDatabase() {
    abstract fun mailAgentDao(): MailAgentDao
    abstract fun emailMessageDao(): EmailMessageDao
    abstract fun agentContextDao(): AgentContextDao
    abstract fun processingJobDao(): ProcessingJobDao

    companion object {
        const val DATABASE_NAME = "mailflow_db"
    }
}
