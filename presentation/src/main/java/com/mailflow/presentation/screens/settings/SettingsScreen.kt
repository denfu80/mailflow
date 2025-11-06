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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val authHelper = viewModel.gmailAuthHelper
    val gmailClient = viewModel.gmailClient
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

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

            Text(
                text = "Active Jobs",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Spacing.medium)
            )

            Text(
                text = "Sync Jobs: ${allSyncWork.size} | Processing Jobs: ${allProcessingWork.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

            Text(
                text = "Account",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Spacing.medium)
            )

            GmailAuthCard(
                authHelper = authHelper,
                gmailClient = gmailClient,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

            Text(
                text = "App Settings",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Spacing.medium)
            )

            SettingsItem(
                title = "Sync Frequency",
                subtitle = "Every 30 minutes",
                onClick = { }
            )

            HorizontalDivider()

            SettingsItem(
                title = "Notifications",
                subtitle = "Enable push notifications",
                onClick = { }
            )

            HorizontalDivider()

            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Spacing.medium)
            )

            SettingsItem(
                title = "Version",
                subtitle = "1.0.0",
                onClick = { }
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

                when (status) {
                    is WorkStatus.Running -> CircularProgressIndicator()
                    else -> {}
                }
            }

            Text(
                text = when (status) {
                    is WorkStatus.Idle -> "No active sync"
                    is WorkStatus.Scheduled -> "Sync scheduled"
                    is WorkStatus.Running -> "Syncing emails..."
                    is WorkStatus.Blocked -> "Sync blocked (waiting for constraints)"
                    is WorkStatus.Cancelled -> "Sync cancelled"
                    is WorkStatus.Succeeded -> "Last sync: ${status.messagesFetched} fetched, ${status.messagesProcessed} processed"
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
