package com.mailflow.data.remote.gmail

import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.usecase.GmailService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailServiceImpl @Inject constructor(
    private val gmailClient: GmailClient
) : GmailService {

    override suspend fun fetchNewMessages(): List<EmailMessage> {
        val result = gmailClient.fetchMessages()

        if (result.isFailure) {
            return emptyList()
        }

        val gmailMessages = result.getOrNull() ?: return emptyList()

        return gmailMessages.map { gmailMessage ->
            EmailMessage(
                id = 0L,
                agentId = 0L,
                messageId = gmailMessage.id,
                subject = gmailMessage.subject,
                sender = gmailMessage.from,
                receivedAt = gmailMessage.date,
                body = gmailMessage.body,
                processed = false
            )
        }
    }
}
