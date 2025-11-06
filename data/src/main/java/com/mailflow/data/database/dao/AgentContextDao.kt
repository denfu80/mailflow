package com.mailflow.data.database.dao

import androidx.room.*
import com.mailflow.data.model.AgentContextEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentContextDao {

    @Query("SELECT * FROM agent_context WHERE agentId = :agentId ORDER BY key ASC")
    fun getContextByAgent(agentId: Long): Flow<List<AgentContextEntity>>

    @Query("SELECT * FROM agent_context WHERE agentId = :agentId AND key = :key")
    suspend fun getContextValue(agentId: Long, key: String): AgentContextEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: AgentContextEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContextBatch(contexts: List<AgentContextEntity>)

    @Update
    suspend fun updateContext(context: AgentContextEntity)

    @Query("DELETE FROM agent_context WHERE agentId = :agentId")
    suspend fun deleteContextByAgent(agentId: Long)

    @Query("DELETE FROM agent_context WHERE agentId = :agentId AND key = :key")
    suspend fun deleteContextByKey(agentId: Long, key: String)
}
