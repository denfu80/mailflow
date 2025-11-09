package com.mailflow.data.repository

import com.mailflow.data.database.dao.ProcessingJobDao
import com.mailflow.data.model.ProcessingJobEntity
import com.mailflow.domain.model.ProcessingLog
import com.mailflow.domain.repository.ProcessingLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProcessingLogRepositoryImpl @Inject constructor(
    private val processingJobDao: ProcessingJobDao
) : ProcessingLogRepository {

    override fun getRecentLogs(): Flow<List<ProcessingLog>> {
        return processingJobDao.getRecentJobs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addLog(log: ProcessingLog) {
        processingJobDao.insertJob(log.toEntity())
    }

    private fun ProcessingJobEntity.toDomainModel(): ProcessingLog {
        return ProcessingLog(
            id = id,
            status = status,
            startTime = startTime,
            endTime = endTime,
            error = error,
            processedCount = processedCount
        )
    }

    private fun ProcessingLog.toEntity(): ProcessingJobEntity {
        return ProcessingJobEntity(
            id = id,
            status = status,
            startTime = startTime,
            endTime = endTime,
            error = error,
            processedCount = processedCount
        )
    }
}
