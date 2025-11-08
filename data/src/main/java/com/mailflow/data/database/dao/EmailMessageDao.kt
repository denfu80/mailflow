package com.mailflow.data.database.dao

import androidx.room.*
import com.mailflow.data.model.EmailMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailMessageDao {

    @Query("SELECT * FROM email_messages WHERE processed = 0 ORDER BY receivedAt ASC")
    fun getUnprocessedMessages(): Flow<List<EmailMessageEntity>>

    @Query("SELECT * FROM email_messages WHERE messageId = :messageId")
    suspend fun getMessageByMessageId(messageId: String): EmailMessageEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: EmailMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessages(messages: List<EmailMessageEntity>): List<Long>

    @Update
    suspend fun updateMessage(message: EmailMessageEntity)

    @Query("UPDATE email_messages SET processed = 1, processedAt = :processedAt WHERE id = :messageId")
    suspend fun markMessageAsProcessed(messageId: Long, processedAt: Long = System.currentTimeMillis())
}
