package com.mailflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "email_messages",
    indices = [
        Index(value = ["messageId"], unique = true),
        Index(value = ["processed"]),
        Index(value = ["selected"])
    ]
)
data class EmailMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: String,
    val subject: String,
    val sender: String,
    val receivedAt: Long,
    val body: String,
    val processed: Boolean = false,
    val processedAt: Long? = null,
    val selected: Boolean = false,
    val extractedTodos: String? = null,
    val todosSynced: Boolean = false
)
