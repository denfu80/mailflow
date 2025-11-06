package com.mailflow.presentation.screens.createagent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mailflow.domain.model.MailAgent
import com.mailflow.domain.repository.AgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAgentViewModel @Inject constructor(
    private val agentRepository: AgentRepository,
    private val validateYamlUseCase: com.mailflow.domain.usecase.ValidateYamlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAgentUiState())
    val uiState: StateFlow<CreateAgentUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = validateName(name)
        )
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onYamlConfigChanged(yaml: String) {
        _uiState.value = _uiState.value.copy(
            yamlConfig = yaml,
            yamlError = validateYaml(yaml)
        )
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 3 -> "Name must be at least 3 characters"
            else -> null
        }
    }

    private fun validateYaml(yaml: String): String? {
        if (yaml.isBlank()) {
            return "YAML configuration cannot be empty"
        }

        val result = validateYamlUseCase(yaml)
        return when (result) {
            is com.mailflow.domain.model.ProcessingResult.Success -> null
            is com.mailflow.domain.model.ProcessingResult.Error -> result.error.message
            is com.mailflow.domain.model.ProcessingResult.Loading -> null
        }
    }

    fun createAgent() {
        val currentState = _uiState.value

        if (!currentState.isValid) {
            _uiState.value = currentState.copy(
                error = "Please fix validation errors"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            val agent = MailAgent(
                name = currentState.name.trim(),
                description = currentState.description.trim(),
                yamlConfig = currentState.yamlConfig.trim(),
                isActive = true
            )

            agentRepository.createAgent(agent)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create agent"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
