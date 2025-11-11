package com.mailflow.presentation.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val todoListName by viewModel.todoListName.collectAsStateWithLifecycle()
    val todoListId by viewModel.todoListId.collectAsStateWithLifecycle()
    val todoListUrl by viewModel.todoListUrl.collectAsStateWithLifecycle()
    val listCreationStatus by viewModel.listCreationStatus.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successUrl by remember { mutableStateOf<String?>(null) }

    // Handle list creation status
    LaunchedEffect(listCreationStatus) {
        when (listCreationStatus) {
            is ListCreationStatus.Success -> {
                successUrl = (listCreationStatus as ListCreationStatus.Success).url
                showSuccessDialog = true
            }
            is ListCreationStatus.Error -> {
                snackbarHostState.showSnackbar(
                    message = (listCreationStatus as ListCreationStatus.Error).message
                )
                viewModel.clearListCreationStatus()
            }
            else -> {}
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearListCreationStatus()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Liste erfolgreich erstellt!") },
            text = {
                Column {
                    Text("Die Todo-Liste wurde erfolgreich erstellt.")
                    if (successUrl != null) {
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = "URL: $successUrl",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                if (successUrl != null) {
                    TextButton(
                        onClick = {
                            copyToClipboard(context, successUrl!!)
                            snackbarHostState.showSnackbar("URL kopiert!")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null
                        )
                        Text("URL kopieren")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearListCreationStatus()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            TodoListSettingsCard(
                listName = todoListName,
                listId = todoListId,
                listUrl = todoListUrl,
                isCreatingList = listCreationStatus is ListCreationStatus.Loading,
                onListNameChange = viewModel::onTodoListNameChange,
                onListIdChange = viewModel::onTodoListIdChange,
                onCreateList = viewModel::createNewList,
                onCopyUrl = { url ->
                    copyToClipboard(context, url)
                    // Show snackbar but we need to launch in coroutine
                },
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )

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
private fun TodoListSettingsCard(
    listName: String,
    listId: String?,
    listUrl: String?,
    isCreatingList: Boolean,
    onListNameChange: (String) -> Unit,
    onListIdChange: (String?) -> Unit,
    onCreateList: () -> Unit,
    onCopyUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            OutlinedTextField(
                value = listName,
                onValueChange = onListNameChange,
                label = { Text("Listen-Name") },
                supportingText = { Text("Name für die neue Todo-Liste") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            OutlinedTextField(
                value = listId ?: "",
                onValueChange = { newValue ->
                    onListIdChange(newValue.ifBlank { null })
                },
                label = { Text("Listen-ID (optional)") },
                supportingText = { Text("ID einer bestehenden Liste verwenden") },
                modifier = Modifier.fillMaxWidth()
            )

            if (listUrl != null) {
                Spacer(modifier = Modifier.height(Spacing.small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Listen-URL",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = listUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onCopyUrl(listUrl) }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "URL kopieren"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Button(
                onClick = onCreateList,
                enabled = !isCreatingList && listName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreatingList) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = Spacing.small)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = Spacing.small)
                    )
                }
                Text("Neue Liste erstellen")
            }

            if (listId != null) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "Aktuelle Listen-ID: $listId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.small)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Todo List URL", text)
    clipboardManager.setPrimaryClip(clip)
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

