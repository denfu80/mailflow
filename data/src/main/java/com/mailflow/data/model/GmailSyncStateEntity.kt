package com.mailflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gmail_sync_state")
data class GmailSyncStateEntity(
    @PrimaryKey
    val id: Int = 1, // Single row table
    val historyId: Long? = null,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
