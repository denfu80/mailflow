package com.mailflow.presentation.screens.dashboard

data class DashboardUiState(
    val agents: List<AgentUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AgentUiModel(
    val id: String,
    val name: String,
    val description: String,
    val isActive: Boolean,
    val unprocessedEmailCount: Int = 0
)
