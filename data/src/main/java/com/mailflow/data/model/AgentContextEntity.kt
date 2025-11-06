package com.mailflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "agent_context",
    foreignKeys = [
        ForeignKey(
            entity = MailAgentEntity::class,
            parentColumns = ["id"],
            childColumns = ["agentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["agentId", "key"], unique = true)
    ]
)
data class AgentContextEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val agentId: Long,
    val key: String,
    val value: String,
    val type: String,
    val updatedAt: Long = System.currentTimeMillis()
)
