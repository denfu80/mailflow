package com.mailflow.domain.usecase

import com.mailflow.domain.model.AgentConfiguration
import com.mailflow.domain.model.ErrorType
import com.mailflow.domain.model.ProcessingError
import com.mailflow.domain.model.ProcessingResult
import javax.inject.Inject

class ValidateYamlUseCase @Inject constructor() {

    operator fun invoke(yamlConfig: String): ProcessingResult<AgentConfiguration> {
        return try {
            if (yamlConfig.isBlank()) {
                return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "YAML configuration cannot be empty"
                    )
                )
            }

            if (!yamlConfig.contains("agent:")) {
                return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "YAML must contain 'agent:' root element"
                    )
                )
            }

            if (!yamlConfig.contains("gemini_prompt:")) {
                return ProcessingResult.Error(
                    ProcessingError(
                        type = ErrorType.VALIDATION_ERROR,
                        message = "YAML must contain 'gemini_prompt:' field"
                    )
                )
            }

            ProcessingResult.Success(
                AgentConfiguration(
                    filters = com.mailflow.domain.model.EmailFilters(),
                    geminiPrompt = "",
                    contextSchema = emptyMap()
                )
            )
        } catch (e: Exception) {
            ProcessingResult.Error(
                ProcessingError(
                    type = ErrorType.PARSING_ERROR,
                    message = "Failed to parse YAML: ${e.message}",
                    cause = e
                )
            )
        }
    }
}
