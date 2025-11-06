package com.mailflow.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mailflow.domain.repository.AgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val agentRepository: AgentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadAgents()
    }

    fun loadAgents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            agentRepository.getAllAgents()
                .map { agents ->
                    agents.map { agent ->
                        AgentUiModel(
                            id = agent.id.toString(),
                            name = agent.name,
                            description = agent.description,
                            isActive = agent.isActive
                        )
                    }
                }
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
                .collect { agentModels ->
                    _uiState.value = _uiState.value.copy(
                        agents = agentModels,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun onRefresh() {
        loadAgents()
    }

    fun deleteAgent(agentId: String) {
        viewModelScope.launch {
            val agent = agentRepository.getAgentById(agentId.toLong())
                .map { it }
                .catch { }
                .firstOrNull()

            agent?.let {
                agentRepository.deleteAgent(it)
                    .onSuccess {
                        loadAgents()
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to delete agent: ${error.message}"
                        )
                    }
            }
        }
    }

    fun toggleAgentActive(agentId: String, isActive: Boolean) {
        viewModelScope.launch {
            agentRepository.updateAgentActiveStatus(agentId.toLong(), isActive)
                .onSuccess {
                    loadAgents()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update agent: ${error.message}"
                    )
                }
        }
    }
}
