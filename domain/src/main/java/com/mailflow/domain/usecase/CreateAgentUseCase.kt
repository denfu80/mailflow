package com.mailflow.domain.usecase

import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.MailAgent
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.repository.AgentRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CreateAgentUseCase @Inject constructor(
    private val agentRepository: AgentRepository,
    private val validateYamlUseCase: ValidateYamlUseCase
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        yamlConfig: String
    ): ProcessingResult<Long> {
        if (name.isBlank()) {
            return ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "Agent name cannot be empty"
                )
            )
        }

        if (name.length < 3) {
            return ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "Agent name must be at least 3 characters"
                )
            )
        }

        if (yamlConfig.isBlank()) {
            return ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "YAML configuration cannot be empty"
                )
            )
        }

        val yamlValidation = validateYamlUseCase(yamlConfig)
        if (yamlValidation is ProcessingResult.Error) {
            return yamlValidation
        }

        val existingAgents = agentRepository.getAllAgents().firstOrNull() ?: emptyList()
        if (existingAgents.any { it.name.equals(name, ignoreCase = true) }) {
            return ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "An agent with the name '$name' already exists"
                )
            )
        }

        val agent = MailAgent(
            name = name.trim(),
            description = description.trim(),
            yamlConfig = yamlConfig,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val result = agentRepository.createAgent(agent)

        return if (result.isSuccess) {
            ProcessingResult.Success(result.getOrThrow())
        } else {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.DATABASE_ERROR,
                    message = "Failed to create agent: ${result.exceptionOrNull()?.message}",
                    cause = result.exceptionOrNull()
                )
            )
        }
    }
}
