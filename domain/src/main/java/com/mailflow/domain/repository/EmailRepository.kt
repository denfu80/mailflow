package com.mailflow.domain.repository

import com.mailflow.domain.model.EmailMessage
import kotlinx.coroutines.flow.Flow

interface EmailRepository {
    fun getMessagesByAgent(agentId: Long): Flow<List<EmailMessage>>
    fun getUnprocessedMessagesByAgent(agentId: Long): Flow<List<EmailMessage>>
    fun getUnprocessedCountByAgent(agentId: Long): Flow<Int>
    suspend fun saveMessage(message: EmailMessage): Result<Long>
    suspend fun saveMessages(messages: List<EmailMessage>): Result<List<Long>>
    suspend fun markMessageAsProcessed(messageId: Long): Result<Unit>
}
