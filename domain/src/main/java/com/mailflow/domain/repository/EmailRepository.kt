package com.mailflow.domain.repository

import com.mailflow.domain.model.EmailMessage
import kotlinx.coroutines.flow.Flow

interface EmailRepository {
    suspend fun fetchNewMessages(): List<EmailMessage>
    fun getUnprocessedMessages(): Flow<List<EmailMessage>>
    fun getAllMessages(): Flow<List<EmailMessage>>
    fun getSelectedMessages(): Flow<List<EmailMessage>>
    fun getMessagesWithUnsyncedTodos(): Flow<List<EmailMessage>>
    suspend fun saveMessages(messages: List<EmailMessage>): Result<List<Long>>
    suspend fun markMessageAsProcessed(messageId: Long): Result<Unit>
    suspend fun updateMessageSelection(messageId: Long, selected: Boolean): Result<Unit>
    suspend fun updateEmailWithTodos(messageId: Long, todos: String?): Result<Unit>
    suspend fun markTodosAsSynced(messageId: Long): Result<Unit>
}
