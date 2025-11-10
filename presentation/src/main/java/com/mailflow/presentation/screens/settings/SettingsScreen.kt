package com.mailflow.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mailflow.domain.model.TodoBackendType
import com.mailflow.presentation.components.molecules.SettingsItem
import com.mailflow.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val syncWorkStatus by viewModel.syncWorkStatus.collectAsStateWithLifecycle()
    val allSyncWork by viewModel.allSyncWork.collectAsStateWithLifecycle()
    val allProcessingWork by viewModel.allProcessingWork.collectAsStateWithLifecycle()
    val todoListName by viewModel.todoListName.collectAsStateWithLifecycle()
    val todoBackendType by viewModel.todoBackendType.collectAsStateWithLifecycle()
    val googleTasksListName by viewModel.googleTasksListName.collectAsStateWithLifecycle()

    val authHelper = viewModel.gmailAuthHelper
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Spacing.medium)
            )

            GmailAuthCard(
                authHelper = authHelper,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

            Text(
                text = "Todo App Settings",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Spacing.medium)
            )

            // Backend Type Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.medium)
                ) {
                    Text(
                        text = "Todo Backend",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = Spacing.small)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.extraSmall)
                    ) {
                        RadioButton(
                            selected = todoBackendType == TodoBackendType.EXTERNAL_API,
                            onClick = { viewModel.onTodoBackendTypeChange(TodoBackendType.EXTERNAL_API) }
                        )
                        Text(
                            text = "External API (doeasy)",
                            modifier = Modifier.padding(start = Spacing.small)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.extraSmall)
                    ) {
                        RadioButton(
                            selected = todoBackendType == TodoBackendType.GOOGLE_TASKS,
                            onClick = { viewModel.onTodoBackendTypeChange(TodoBackendType.GOOGLE_TASKS) }
                        )
                        Text(
                            text = "Google Tasks",
                            modifier = Modifier.padding(start = Spacing.small)
                        )
                    }
                }
            }

            // Backend-specific configuration
            when (todoBackendType) {
                TodoBackendType.EXTERNAL_API -> {
                    OutlinedTextField(
                        value = todoListName,
                        onValueChange = viewModel::onTodoListNameChange,
                        label = { Text("Todo List Name/ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.medium)
                            .padding(top = Spacing.medium)
                    )
                }
                TodoBackendType.GOOGLE_TASKS -> {
                    OutlinedTextField(
                        value = googleTasksListName,
                        onValueChange = viewModel::onGoogleTasksListNameChange,
                        label = { Text("Google Tasks List Name") },
                        supportingText = { Text("The name of your Google Tasks list (e.g., 'My Tasks')") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.medium)
                            .padding(top = Spacing.medium)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

            Text(
                text = "Background Sync",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Spacing.medium)
            )

            WorkStatusCard(
                status = syncWorkStatus,
                onManualSync = { viewModel.triggerManualSync() },
                onCancelAll = { viewModel.cancelAllWork() },
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
        }
    }
}

@Composable
private fun WorkStatusCard(
    status: WorkStatus,
    onManualSync: () -> Unit,
    onCancelAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sync Status",
                    style = MaterialTheme.typography.titleMedium
                )

                if (status is WorkStatus.Running) {
                    CircularProgressIndicator()
                }
            }

            Text(
                text = when (status) {
                    is WorkStatus.Idle -> "No active sync"
                    is WorkStatus.Scheduled -> "Sync scheduled"
                    is WorkStatus.Running -> "Syncing emails..."
                    is WorkStatus.Blocked -> "Sync blocked (waiting for constraints)"
                    is WorkStatus.Cancelled -> "Sync cancelled"
                    is WorkStatus.Succeeded -> "Last sync succeeded"
                    is WorkStatus.Failed -> "Sync failed: ${status.error}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (status) {
                    is WorkStatus.Failed -> MaterialTheme.colorScheme.error
                    is WorkStatus.Succeeded -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(vertical = Spacing.small)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Button(
                    onClick = onManualSync,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Text("Sync Now")
                }

                OutlinedButton(
                    onClick = onCancelAll,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel All")
                }
            }
        }
    }
}

