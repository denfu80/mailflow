package com.mailflow.data.database.dao

import androidx.room.*
import com.mailflow.data.model.ProcessingJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessingJobDao {

    @Query("SELECT * FROM processing_jobs WHERE agentId = :agentId ORDER BY startTime DESC LIMIT :limit")
    fun getJobsByAgent(agentId: Long, limit: Int = 10): Flow<List<ProcessingJobEntity>>

    @Query("SELECT * FROM processing_jobs WHERE status = 'RUNNING' ORDER BY startTime ASC")
    suspend fun getRunningJobs(): List<ProcessingJobEntity>

    @Insert
    suspend fun insertJob(job: ProcessingJobEntity): Long

    @Update
    suspend fun updateJob(job: ProcessingJobEntity)

    @Query("UPDATE processing_jobs SET status = :status, endTime = :endTime WHERE id = :jobId")
    suspend fun updateJobStatus(jobId: Long, status: String, endTime: Long = System.currentTimeMillis())

    @Query("UPDATE processing_jobs SET status = :status, endTime = :endTime, error = :error WHERE id = :jobId")
    suspend fun updateJobWithError(jobId: Long, status: String, error: String, endTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM processing_jobs WHERE agentId = :agentId")
    suspend fun deleteJobsByAgent(agentId: Long)
}
