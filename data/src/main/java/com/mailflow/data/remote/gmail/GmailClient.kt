package com.mailflow.data.remote.gmail

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailClient @Inject constructor() {

    suspend fun authenticateUser(): Result<String> {
        return Result.failure(NotImplementedError("Gmail OAuth implementation pending - requires Google Sign-In SDK"))
    }

    suspend fun fetchMessages(
        query: String? = null,
        maxResults: Int = 100
    ): Result<List<GmailMessage>> {
        return Result.failure(NotImplementedError("Gmail API implementation pending"))
    }

    suspend fun getMessage(messageId: String): Result<GmailMessage> {
        return Result.failure(NotImplementedError("Gmail API implementation pending"))
    }

    suspend fun markAsRead(messageId: String): Result<Unit> {
        return Result.failure(NotImplementedError("Gmail API implementation pending"))
    }

    data class GmailMessage(
        val id: String,
        val threadId: String,
        val subject: String,
        val from: String,
        val to: List<String>,
        val date: Long,
        val body: String,
        val snippet: String,
        val labelIds: List<String>,
        val hasAttachments: Boolean
    )
}
