package com.mailflow.data.database.dao

import androidx.room.*
import com.mailflow.data.model.EmailMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailMessageDao {

    @Query("SELECT * FROM email_messages WHERE processed = 0 ORDER BY receivedAt ASC")
    fun getUnprocessedMessages(): Flow<List<EmailMessageEntity>>

    @Query("SELECT * FROM email_messages ORDER BY receivedAt DESC")
    fun getAllMessages(): Flow<List<EmailMessageEntity>>

    @Query("SELECT * FROM email_messages WHERE selected = 1 ORDER BY receivedAt DESC")
    fun getSelectedMessages(): Flow<List<EmailMessageEntity>>

    @Query("SELECT * FROM email_messages WHERE extractedTodos IS NOT NULL AND todosSynced = 0")
    fun getMessagesWithUnsyncedTodos(): Flow<List<EmailMessageEntity>>

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

    @Query("UPDATE email_messages SET selected = :selected WHERE id = :messageId")
    suspend fun updateMessageSelection(messageId: Long, selected: Boolean)

    @Query("UPDATE email_messages SET extractedTodos = :todos, processed = 1, processedAt = :processedAt WHERE id = :messageId")
    suspend fun updateMessageWithTodos(messageId: Long, todos: String?, processedAt: Long = System.currentTimeMillis())

    @Query("UPDATE email_messages SET todosSynced = 1 WHERE id = :messageId")
    suspend fun markTodosAsSynced(messageId: Long)
}
