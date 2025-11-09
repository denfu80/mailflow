package com.mailflow.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mailflow.data.database.dao.*
import com.mailflow.data.model.*

@Database(
    entities = [
        EmailMessageEntity::class,
        ProcessingJobEntity::class,
        GmailSyncStateEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class MailFlowDatabase : RoomDatabase() {
    abstract fun emailMessageDao(): EmailMessageDao
    abstract fun processingJobDao(): ProcessingJobDao
    abstract fun gmailSyncStateDao(): GmailSyncStateDao

    companion object {
        const val DATABASE_NAME = "mailflow_db"
    }
}
