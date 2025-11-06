package com.mailflow.domain.repository

import com.mailflow.domain.model.MailAgent
import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    fun getAllAgents(): Flow<List<MailAgent>>
    fun getActiveAgents(): Flow<List<MailAgent>>
    fun getAgentById(agentId: Long): Flow<MailAgent?>
    suspend fun createAgent(agent: MailAgent): Result<Long>
    suspend fun updateAgent(agent: MailAgent): Result<Unit>
    suspend fun deleteAgent(agent: MailAgent): Result<Unit>
    suspend fun updateAgentActiveStatus(agentId: Long, isActive: Boolean): Result<Unit>
}
