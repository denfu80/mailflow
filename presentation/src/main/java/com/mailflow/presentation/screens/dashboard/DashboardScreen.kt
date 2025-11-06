package com.mailflow.presentation.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mailflow.presentation.components.atoms.EmptyState
import com.mailflow.presentation.components.atoms.ErrorState
import com.mailflow.presentation.components.atoms.LoadingIndicator
import com.mailflow.presentation.components.molecules.AgentCard
import com.mailflow.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAgentClick: (String) -> Unit,
    onCreateAgentClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MailFlow Agents") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateAgentClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Agent"
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.onRefresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.error != null -> {
                        ErrorState(
                            message = uiState.error ?: "Unknown error",
                            onRetry = { viewModel.onRefresh() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.agents.isEmpty() && !uiState.isLoading -> {
                        EmptyState(
                            message = "No agents yet",
                            subtitle = "Create your first agent to get started",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                        ) {
                            items(
                                items = uiState.agents,
                                key = { it.id }
                            ) { agent ->
                                AgentCard(
                                    name = agent.name,
                                    description = agent.description,
                                    isActive = agent.isActive,
                                    onClick = { onAgentClick(agent.id) },
                                    onEdit = { /* TODO: Navigate to edit */ },
                                    onDelete = { viewModel.deleteAgent(agent.id) },
                                    onToggleActive = { isActive ->
                                        viewModel.toggleAgentActive(agent.id, isActive)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
