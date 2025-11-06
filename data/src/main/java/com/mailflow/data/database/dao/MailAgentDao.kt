package com.mailflow.data.database.dao

import androidx.room.*
import com.mailflow.data.model.MailAgentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MailAgentDao {

    @Query("SELECT * FROM mail_agents ORDER BY createdAt DESC")
    fun getAllAgents(): Flow<List<MailAgentEntity>>

    @Query("SELECT * FROM mail_agents WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveAgents(): Flow<List<MailAgentEntity>>

    @Query("SELECT * FROM mail_agents WHERE id = :agentId")
    fun getAgentById(agentId: Long): Flow<MailAgentEntity?>

    @Query("SELECT * FROM mail_agents WHERE id = :agentId")
    suspend fun getAgentByIdOnce(agentId: Long): MailAgentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAgent(agent: MailAgentEntity): Long

    @Update
    suspend fun updateAgent(agent: MailAgentEntity)

    @Delete
    suspend fun deleteAgent(agent: MailAgentEntity)

    @Query("UPDATE mail_agents SET isActive = :isActive, updatedAt = :timestamp WHERE id = :agentId")
    suspend fun updateAgentActiveStatus(agentId: Long, isActive: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE mail_agents SET yamlConfig = :yamlConfig, updatedAt = :timestamp WHERE id = :agentId")
    suspend fun updateAgentConfig(agentId: Long, yamlConfig: String, timestamp: Long = System.currentTimeMillis())
}
