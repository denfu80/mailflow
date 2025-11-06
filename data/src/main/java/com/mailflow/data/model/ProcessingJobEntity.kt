package com.mailflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "processing_jobs",
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
        Index(value = ["status"])
    ]
)
data class ProcessingJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val agentId: Long,
    val status: String,
    val startTime: Long,
    val endTime: Long? = null,
    val error: String? = null,
    val processedCount: Int = 0
)
