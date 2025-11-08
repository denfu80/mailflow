package com.mailflow.data.repository

import com.mailflow.core.di.IoDispatcher
import com.mailflow.data.database.dao.EmailMessageDao
import com.mailflow.data.model.EmailMessageEntity
import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.repository.EmailRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmailRepositoryImpl @Inject constructor(
    private val dao: EmailMessageDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
    // private val gmailService: GmailService // Will be added later
) : EmailRepository {

    override suspend fun fetchNewMessages(): List<EmailMessage> {
        // TODO: Implement with GmailService
        return emptyList()
    }

    override fun getUnprocessedMessages(): Flow<List<EmailMessage>> {
        return dao.getUnprocessedMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveMessages(messages: List<EmailMessage>): Result<List<Long>> = withContext(ioDispatcher) {
        try {
            val ids = dao.insertMessages(messages.map { it.toEntity() })
            Result.success(ids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMessageAsProcessed(messageId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            dao.markMessageAsProcessed(messageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun EmailMessageEntity.toDomain() = EmailMessage(
        id = id,
        messageId = messageId,
        subject = subject,
        sender = sender,
        receivedAt = receivedAt,
        body = body,
        processed = processed,
        processedAt = processedAt
    )

    private fun EmailMessage.toEntity() = EmailMessageEntity(
        id = id,
        messageId = messageId,
        subject = subject,
        sender = sender,
        receivedAt = receivedAt,
        body = body,
        processed = processed,
        processedAt = processedAt
    )
}

