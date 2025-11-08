package com.mailflow.domain.repository

import com.mailflow.domain.model.ProcessingLog
import kotlinx.coroutines.flow.Flow

interface ProcessingLogRepository {
    fun getRecentLogs(): Flow<List<ProcessingLog>>
    suspend fun addLog(log: ProcessingLog)
}
