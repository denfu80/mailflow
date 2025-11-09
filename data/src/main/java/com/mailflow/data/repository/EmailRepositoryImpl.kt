package com.mailflow.data.repository

import android.util.Log
import com.mailflow.core.di.IoDispatcher
import com.mailflow.data.database.dao.EmailMessageDao
import com.mailflow.data.database.dao.GmailSyncStateDao
import com.mailflow.data.model.EmailMessageEntity
import com.mailflow.data.model.GmailSyncStateEntity
import com.mailflow.data.remote.gmail.GmailClient
import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.repository.EmailRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmailRepositoryImpl @Inject constructor(
    private val dao: EmailMessageDao,
    private val gmailSyncStateDao: GmailSyncStateDao,
    private val gmailClient: GmailClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : EmailRepository {

    companion object {
        private const val TAG = "EmailRepositoryImpl"
        private const val ONE_DAY_QUERY = "newer_than:1d"
    }

    override suspend fun fetchNewMessages(): List<EmailMessage> = withContext(ioDispatcher) {
        try {
            // Check if authenticated
            if (!gmailClient.isAuthenticated()) {
                Log.w(TAG, "Not authenticated with Gmail")
                return@withContext emptyList()
            }

            // Fetch messages from the last 24 hours
            val result = gmailClient.fetchMessages(
                query = ONE_DAY_QUERY,
                maxResults = 100
            ).getOrElse { exception ->
                Log.e(TAG, "Failed to fetch messages from Gmail: ${exception.message}", exception)
                return@withContext emptyList()
            }

            Log.d(TAG, "Fetched ${result.messages.size} messages from Gmail")

            // Update history ID for future syncs
            result.historyId?.let { historyId ->
                gmailSyncStateDao.updateSyncState(
                    GmailSyncStateEntity(
                        id = 1,
                        historyId = historyId.toLong(),
                        lastSyncTimestamp = System.currentTimeMillis()
                    )
                )
                Log.d(TAG, "Updated history ID: $historyId")
            }

            // Convert to domain model
            result.messages.map { gmailMessage ->
                EmailMessage(
                    id = 0, // Will be auto-generated
                    messageId = gmailMessage.id,
                    subject = gmailMessage.subject,
                    sender = gmailMessage.from,
                    receivedAt = gmailMessage.date,
                    body = gmailMessage.body,
                    processed = false,
                    processedAt = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchNewMessages: ${e.message}", e)
            emptyList()
        }
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

