package com.mailflow.data.repository

import com.mailflow.core.di.IoDispatcher
import com.mailflow.data.database.dao.AgentContextDao
import com.mailflow.data.model.AgentContextEntity
import com.mailflow.domain.model.AgentContext
import com.mailflow.domain.repository.ContextRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContextRepositoryImpl @Inject constructor(
    private val dao: AgentContextDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContextRepository {

    override fun getContextByAgent(agentId: Long): Flow<List<AgentContext>> {
        return dao.getContextByAgent(agentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getContextValue(agentId: Long, key: String): AgentContext? = withContext(ioDispatcher) {
        dao.getContextValue(agentId, key)?.toDomain()
    }

    override suspend fun saveContext(context: AgentContext): Result<Unit> = withContext(ioDispatcher) {
        try {
            dao.insertContext(context.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveContextBatch(contexts: List<AgentContext>): Result<Unit> = withContext(ioDispatcher) {
        try {
            dao.insertContextBatch(contexts.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteContextByAgent(agentId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            dao.deleteContextByAgent(agentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun AgentContextEntity.toDomain() = AgentContext(
        id = id,
        agentId = agentId,
        key = key,
        value = value,
        type = type,
        updatedAt = updatedAt
    )

    private fun AgentContext.toEntity() = AgentContextEntity(
        id = id,
        agentId = agentId,
        key = key,
        value = value,
        type = type,
        updatedAt = updatedAt
    )
}
