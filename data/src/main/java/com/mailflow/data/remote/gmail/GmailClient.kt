package com.mailflow.data.remote.gmail

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailClient @Inject constructor(
    private val gmailApiClient: GmailApiClient
) {

    fun isAuthenticated(): Boolean {
        return gmailApiClient.isSignedIn()
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        return gmailApiClient.getSignedInAccount()
    }

    fun initializeWithAccount(account: GoogleSignInAccount) {
        gmailApiClient.initializeService(account)
    }

    data class FetchMessagesResult(
        val messages: List<GmailMessage>,
        val historyId: java.math.BigInteger?
    )

    suspend fun fetchMessages(
        query: String? = null,
        maxResults: Int = 100
    ): Result<FetchMessagesResult> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated. Please sign in first."))
        }

        return gmailApiClient.fetchMessages(query, maxResults.toLong()).map { result ->
            FetchMessagesResult(result.messages, result.historyId)
        }
    }

    suspend fun getMessage(messageId: String): Result<GmailMessage> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated"))
        }

        val result = fetchMessages(query = "rfc822msgid:$messageId", maxResults = 1)
        return result.mapCatching { it.messages.firstOrNull() ?: throw NoSuchElementException("Message not found") }
    }

    suspend fun markAsRead(messageId: String): Result<Unit> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated"))
        }

        return gmailApiClient.markAsRead(messageId)
    }

    fun signOut() {
        gmailApiClient.signOut()
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
