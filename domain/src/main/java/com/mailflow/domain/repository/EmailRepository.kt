package com.mailflow.domain.repository

import com.mailflow.domain.model.EmailMessage
import kotlinx.coroutines.flow.Flow

interface EmailRepository {
    suspend fun fetchNewMessages(): List<EmailMessage>
    fun getUnprocessedMessages(): Flow<List<EmailMessage>>
    suspend fun saveMessages(messages: List<EmailMessage>): Result<List<Long>>
    suspend fun markMessageAsProcessed(messageId: Long): Result<Unit>
}
