package com.mailflow.domain.repository

import com.mailflow.domain.model.AgentContext
import kotlinx.coroutines.flow.Flow

interface ContextRepository {
    fun getContextByAgent(agentId: Long): Flow<List<AgentContext>>
    suspend fun getContextValue(agentId: Long, key: String): AgentContext?
    suspend fun saveContext(context: AgentContext): Result<Unit>
    suspend fun saveContextBatch(contexts: List<AgentContext>): Result<Unit>
    suspend fun deleteContextByAgent(agentId: Long): Result<Unit>
}
