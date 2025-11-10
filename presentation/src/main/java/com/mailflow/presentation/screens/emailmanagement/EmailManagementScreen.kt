package com.mailflow.presentation.screens.emailmanagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.max
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mailflow.presentation.components.atoms.EmptyState
import com.mailflow.presentation.components.atoms.LoadingIndicator
import com.mailflow.presentation.components.molecules.EmailListItem
import com.mailflow.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailManagementScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmailManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emails by viewModel.emails.collectAsStateWithLifecycle()
    val todoListName by viewModel.todoListName.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for messages
    LaunchedEffect(
        uiState.lastSyncMessage,
        uiState.lastAnalysisMessage,
        uiState.lastSyncTodosMessage,
        uiState.syncError,
        uiState.analysisError,
        uiState.syncTodosError
    ) {
        val message = uiState.syncError
            ?: uiState.analysisError
            ?: uiState.syncTodosError
            ?: uiState.lastSyncMessage
            ?: uiState.lastAnalysisMessage
            ?: uiState.lastSyncTodosMessage

        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E-Mail Verwaltung") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Einstellungen"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            EmailManagementBottomBar(
                selectedCount = uiState.selectedCount,
                isSyncing = uiState.isSyncing,
                isAnalyzing = uiState.isAnalyzing,
                isSyncingTodos = uiState.isSyncingTodos,
                hasUnsyncedTodos = emails.any { !it.extractedTodos.isNullOrBlank() && !it.todosSynced },
                onSyncEmails = viewModel::syncEmails,
                onAnalyzeSelected = viewModel::analyzeSelectedEmails,
                onAnalyzeAll = viewModel::analyzeAllEmails,
                onSyncTodos = viewModel::syncTodosToList,
                todoListName = todoListName
            )
        },
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                emails.isEmpty() -> {
                    EmptyState(
                        message = "Keine E-Mails vorhanden.\nKlicken Sie auf 'E-Mails syncen' um zu beginnen.",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // Email Statistics
                    EmailStatisticsCard(
                        totalEmails = emails.size,
                        analyzedEmails = emails.count { it.processed },
                        emailsWithTodos = emails.count { !it.extractedTodos.isNullOrBlank() },
                        syncedTodos = emails.count { it.todosSynced },
                        modifier = Modifier.padding(Spacing.medium)
                    )

                    // Email List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = emails,
                            key = { it.id }
                        ) { email ->
                            EmailListItem(
                                email = email,
                                onSelectionToggle = viewModel::toggleEmailSelection,
                                onAnalyze = viewModel::analyzeSingleEmail
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailStatisticsCard(
    totalEmails: Int,
    analyzedEmails: Int,
    emailsWithTodos: Int,
    syncedTodos: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem("Gesamt", totalEmails.toString())
            StatisticItem("Analysiert", "$analyzedEmails/$totalEmails")
            StatisticItem("TODOs", emailsWithTodos.toString())
            StatisticItem("Synchronisiert", "$syncedTodos/$emailsWithTodos")
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EmailManagementBottomBar(
    selectedCount: Int,
    isSyncing: Boolean,
    isAnalyzing: Boolean,
    isSyncingTodos: Boolean,
    hasUnsyncedTodos: Boolean,
    onSyncEmails: () -> Unit,
    onAnalyzeSelected: () -> Unit,
    onAnalyzeAll: () -> Unit,
    onSyncTodos: () -> Unit,
    todoListName: String,
    modifier: Modifier = Modifier
) {
    var showAnalyzeMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shadowElevation = 8.dp,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
        ) {
            // First Row: Sync and Analyze buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sync Button
                Button(
                    onClick = onSyncEmails,
                    enabled = !isSyncing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("E-Mails syncen")
                }

                // Analyze Button with Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showAnalyzeMenu = true },
                        enabled = !isAnalyzing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analysieren")
                    }

                    DropdownMenu(
                        expanded = showAnalyzeMenu,
                        onDismissRequest = { showAnalyzeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Markierte ($selectedCount)") },
                            onClick = {
                                showAnalyzeMenu = false
                                onAnalyzeSelected()
                            },
                            enabled = selectedCount > 0
                        )
                        DropdownMenuItem(
                            text = { Text("Alle nicht analysierten") },
                            onClick = {
                                showAnalyzeMenu = false
                                onAnalyzeAll()
                            }
                        )
                    }
                }
            }

            // Second Row: TODO Sync
            if (hasUnsyncedTodos) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSyncTodos,
                    enabled = !isSyncingTodos,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    if (isSyncingTodos) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TODOs zu '$todoListName' synchronisieren")
                }
            }
        }
    }
}
