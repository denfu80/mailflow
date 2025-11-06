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

    suspend fun fetchMessages(
        query: String? = null,
        maxResults: Int = 100
    ): Result<List<GmailMessage>> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated. Please sign in first."))
        }

        return gmailApiClient.fetchMessages(query, maxResults.toLong())
    }

    suspend fun getMessage(messageId: String): Result<GmailMessage> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated"))
        }

        val messages = fetchMessages(query = "rfc822msgid:$messageId", maxResults = 1)
        return messages.mapCatching { it.firstOrNull() ?: throw NoSuchElementException("Message not found") }
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
