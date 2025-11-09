package com.mailflow.data.remote.gmail

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailApiClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var gmailService: Gmail? = null

    companion object {
        private const val TAG = "GmailApiClient"
    }

    fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(GmailScopes.GMAIL_READONLY),
                Scope(GmailScopes.GMAIL_MODIFY)
            )
            .build()
    }

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && hasGmailScope(account)
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    private fun hasGmailScope(account: GoogleSignInAccount): Boolean {
        return account.grantedScopes.contains(Scope(GmailScopes.GMAIL_READONLY))
    }

    fun initializeService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_MODIFY)
        ).apply {
            selectedAccount = account.account
        }

        gmailService = Gmail.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("MailFlow")
            .build()
    }

    data class FetchMessagesResult(
        val messages: List<GmailClient.GmailMessage>,
        val historyId: Long?
    )

    suspend fun fetchMessages(
        query: String? = null,
        maxResults: Long = 100
    ): Result<FetchMessagesResult> = withContext(Dispatchers.IO) {
        try {
            var service = gmailService

            if (service == null) {
                Log.d(TAG, "Gmail service is null, attempting to re-initialize")
                val account = getSignedInAccount()
                if (account != null) {
                    Log.d(TAG, "Found signed-in account: ${account.email}")
                    initializeService(account)
                    service = gmailService
                } else {
                    Log.w(TAG, "No signed-in account found")
                }
            }

            if (service == null) {
                Log.e(TAG, "Gmail service could not be initialized")
                return@withContext Result.failure(IllegalStateException("Gmail service not initialized. Please sign in first."))
            }

            Log.d(TAG, "Fetching messages with query: $query, maxResults: $maxResults")
            val messagesResponse = service.users()
                .messages()
                .list("me")
                .apply {
                    q = query
                    setMaxResults(maxResults)
                }
                .execute()

            val messages = messagesResponse.messages
            val historyId = messagesResponse.resultSizeEstimate?.let {
                // Get the latest history ID from the profile
                try {
                    service.users().getProfile("me").execute().historyId
                } catch (e: Exception) {
                    Log.w(TAG, "Could not fetch history ID: ${e.message}")
                    null
                }
            }
            Log.d(TAG, "Fetched ${messages?.size ?: 0} message IDs from Gmail API, historyId: $historyId")

            if (messages == null) {
                return@withContext Result.success(FetchMessagesResult(emptyList(), historyId))
            }

            val detailedMessages = messages.mapNotNull { message ->
                try {
                    val fullMessage = service.users()
                        .messages()
                        .get("me", message.id)
                        .setFormat("full")
                        .execute()

                    val headers = fullMessage.payload?.headers
                    val subject = headers?.find { it.name.equals("Subject", ignoreCase = true) }?.value ?: "(No Subject)"
                    val from = headers?.find { it.name.equals("From", ignoreCase = true) }?.value ?: "Unknown"
                    val to = headers?.find { it.name.equals("To", ignoreCase = true) }?.value?.split(",") ?: emptyList()
                    val date = fullMessage.internalDate ?: System.currentTimeMillis()

                    val body = extractBody(fullMessage.payload)
                    val snippet = fullMessage.snippet ?: ""
                    val labelIds = fullMessage.labelIds ?: emptyList()
                    val hasAttachments = fullMessage.payload?.parts?.any {
                        it.filename?.isNotEmpty() == true
                    } ?: false

                    GmailClient.GmailMessage(
                        id = fullMessage.id,
                        threadId = fullMessage.threadId,
                        subject = subject,
                        from = from,
                        to = to,
                        date = date,
                        body = body,
                        snippet = snippet,
                        labelIds = labelIds,
                        hasAttachments = hasAttachments
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching message details: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "Successfully fetched ${detailedMessages.size} detailed messages")
            Result.success(FetchMessagesResult(detailedMessages, historyId))
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchMessages: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun markAsRead(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            var service = gmailService

            if (service == null) {
                val account = getSignedInAccount()
                if (account != null) {
                    initializeService(account)
                    service = gmailService
                }
            }

            if (service == null) {
                return@withContext Result.failure(IllegalStateException("Gmail service not initialized"))
            }

            val modifyRequest = com.google.api.services.gmail.model.ModifyMessageRequest()
                .setRemoveLabelIds(listOf("UNREAD"))

            service.users()
                .messages()
                .modify("me", messageId, modifyRequest)
                .execute()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractBody(payload: com.google.api.services.gmail.model.MessagePart?): String {
        if (payload == null) return ""

        if (payload.body?.data != null) {
            return String(android.util.Base64.decode(payload.body.data, android.util.Base64.URL_SAFE))
        }

        val textPart = payload.parts?.find { it.mimeType == "text/plain" }
        if (textPart?.body?.data != null) {
            return String(android.util.Base64.decode(textPart.body.data, android.util.Base64.URL_SAFE))
        }

        val htmlPart = payload.parts?.find { it.mimeType == "text/html" }
        if (htmlPart?.body?.data != null) {
            return String(android.util.Base64.decode(htmlPart.body.data, android.util.Base64.URL_SAFE))
        }

        return payload.parts?.joinToString("\n") { extractBody(it) } ?: ""
    }

    fun signOut() {
        gmailService = null
    }
}
