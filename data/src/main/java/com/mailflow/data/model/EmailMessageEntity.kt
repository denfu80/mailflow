package com.mailflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "email_messages",
    foreignKeys = [
        ForeignKey(
            entity = MailAgentEntity::class,
            parentColumns = ["id"],
            childColumns = ["agentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["agentId"]),
        Index(value = ["messageId"], unique = true),
        Index(value = ["processed"])
    ]
)
data class EmailMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val agentId: Long,
    val messageId: String,
    val subject: String,
    val sender: String,
    val receivedAt: Long,
    val body: String,
    val processed: Boolean = false,
    val processedAt: Long? = null
)
