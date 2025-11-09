package com.mailflow.data.database.dao

import androidx.room.*
import com.mailflow.data.model.GmailSyncStateEntity

@Dao
interface GmailSyncStateDao {

    @Query("SELECT * FROM gmail_sync_state WHERE id = 1")
    suspend fun getSyncState(): GmailSyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSyncState(state: GmailSyncStateEntity)

    @Query("UPDATE gmail_sync_state SET historyId = :historyId, lastSyncTimestamp = :timestamp WHERE id = 1")
    suspend fun updateHistoryId(historyId: Long, timestamp: Long = System.currentTimeMillis())
}
