package com.mailflow.data.repository

import com.mailflow.core.di.IoDispatcher
import com.mailflow.data.database.dao.MailAgentDao
import com.mailflow.data.model.MailAgentEntity
import com.mailflow.domain.model.MailAgent
import com.mailflow.domain.repository.AgentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AgentRepositoryImpl @Inject constructor(
    private val dao: MailAgentDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AgentRepository {

    override fun getAllAgents(): Flow<List<MailAgent>> {
        return dao.getAllAgents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveAgents(): Flow<List<MailAgent>> {
        return dao.getActiveAgents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAgentById(agentId: Long): Flow<MailAgent?> {
        return dao.getAgentById(agentId).map { it?.toDomain() }
    }

    override suspend fun createAgent(agent: MailAgent): Result<Long> = withContext(ioDispatcher) {
        try {
            val id = dao.insertAgent(agent.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAgent(agent: MailAgent): Result<Unit> = withContext(ioDispatcher) {
        try {
            dao.updateAgent(agent.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAgent(agent: MailAgent): Result<Unit> = withContext(ioDispatcher) {
        try {
            dao.deleteAgent(agent.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAgentActiveStatus(agentId: Long, isActive: Boolean): Result<Unit> = withContext(ioDispatcher) {
        try {
            dao.updateAgentActiveStatus(agentId, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun MailAgentEntity.toDomain() = MailAgent(
        id = id,
        name = name,
        description = description,
        yamlConfig = yamlConfig,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun MailAgent.toEntity() = MailAgentEntity(
        id = id,
        name = name,
        description = description,
        yamlConfig = yamlConfig,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
