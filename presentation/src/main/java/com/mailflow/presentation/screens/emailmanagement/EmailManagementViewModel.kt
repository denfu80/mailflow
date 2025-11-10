package com.mailflow.presentation.screens.emailmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mailflow.domain.model.EmailMessage
import com.mailflow.domain.model.ProcessingResult
import com.mailflow.domain.repository.EmailRepository
import com.mailflow.domain.usecase.AnalyzeEmailsUseCase
import com.mailflow.domain.usecase.SyncEmailsUseCase
import com.mailflow.domain.usecase.SyncTodosToListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailManagementViewModel @Inject constructor(
    private val emailRepository: EmailRepository,
    private val syncEmailsUseCase: SyncEmailsUseCase,
    private val analyzeEmailsUseCase: AnalyzeEmailsUseCase,
    private val syncTodosToListUseCase: SyncTodosToListUseCase,
    private val settingsDataStore: com.mailflow.data.preferences.SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailManagementUiState())
    val uiState: StateFlow<EmailManagementUiState> = _uiState.asStateFlow()

    val emails: StateFlow<List<EmailMessage>> = emailRepository.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todoListName: StateFlow<String> = settingsDataStore.todoListName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "inbox-test"
        )

    init {
        observeEmailsForSelection()
    }

    private fun observeEmailsForSelection() {
        viewModelScope.launch {
            emails.collect { emailList ->
                val selectedCount = emailList.count { it.selected }
                _uiState.update { it.copy(selectedCount = selectedCount) }
            }
        }
    }

    fun syncEmails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncError = null) }

            val result = syncEmailsUseCase()

            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncError = if (result is com.mailflow.domain.model.SyncResult.Failure) {
                        result.error.message
                    } else null,
                    lastSyncMessage = when (result) {
                        is com.mailflow.domain.model.SyncResult.Success ->
                            "${result.messagesFetched} E-Mails synchronisiert"
                        is com.mailflow.domain.model.SyncResult.PartialSuccess ->
                            "${result.messagesFetched} E-Mails synchronisiert (${result.errors} Fehler)"
                        is com.mailflow.domain.model.SyncResult.Failure ->
                            "Synchronisation fehlgeschlagen"
                    }
                )
            }
        }
    }

    fun toggleEmailSelection(emailId: Long) {
        viewModelScope.launch {
            val email = emails.value.find { it.id == emailId } ?: return@launch
            emailRepository.updateMessageSelection(emailId, !email.selected)
        }
    }

    fun analyzeSelectedEmails() {
        viewModelScope.launch {
            val selectedEmails = emails.value.filter { it.selected && !it.processed }
            if (selectedEmails.isEmpty()) {
                _uiState.update { it.copy(analysisError = "Keine nicht-analysierten E-Mails ausgewählt") }
                return@launch
            }

            _uiState.update { it.copy(isAnalyzing = true, analysisError = null) }

            val result = analyzeEmailsUseCase.analyzeMultiple(selectedEmails)

            when (result) {
                is ProcessingResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            lastAnalysisMessage = "${result.data.successful}/${result.data.total} E-Mails analysiert"
                        )
                    }
                }
                is ProcessingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            analysisError = result.error.message
                        )
                    }
                }
                is ProcessingResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun analyzeAllEmails() {
        viewModelScope.launch {
            val unprocessedEmails = emails.value.filter { !it.processed }
            if (unprocessedEmails.isEmpty()) {
                _uiState.update { it.copy(analysisError = "Keine nicht-analysierten E-Mails vorhanden") }
                return@launch
            }

            _uiState.update { it.copy(isAnalyzing = true, analysisError = null) }

            val result = analyzeEmailsUseCase.analyzeMultiple(unprocessedEmails)

            when (result) {
                is ProcessingResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            lastAnalysisMessage = "${result.data.successful}/${result.data.total} E-Mails analysiert"
                        )
                    }
                }
                is ProcessingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            analysisError = result.error.message
                        )
                    }
                }
                is ProcessingResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun analyzeSingleEmail(emailId: Long) {
        viewModelScope.launch {
            val email = emails.value.find { it.id == emailId } ?: return@launch

            _uiState.update { it.copy(isAnalyzing = true, analysisError = null) }

            val result = analyzeEmailsUseCase.analyzeSingle(email)

            when (result) {
                is ProcessingResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            lastAnalysisMessage = result.data
                        )
                    }
                }
                is ProcessingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            analysisError = result.error.message
                        )
                    }
                }
                is ProcessingResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun syncTodosToList() {
        viewModelScope.launch {
            val emailsWithUnsyncedTodos = emails.value.filter {
                !it.extractedTodos.isNullOrBlank() && !it.todosSynced
            }

            if (emailsWithUnsyncedTodos.isEmpty()) {
                _uiState.update { it.copy(syncTodosError = "Keine nicht-synchronisierten TODOs vorhanden") }
                return@launch
            }

            _uiState.update { it.copy(isSyncingTodos = true, syncTodosError = null) }

            val listName = todoListName.value
            val result = syncTodosToListUseCase.syncMultiple(emailsWithUnsyncedTodos, listName)

            when (result) {
                is ProcessingResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSyncingTodos = false,
                            lastSyncTodosMessage = "${result.data.successful}/${result.data.total} TODOs synchronisiert"
                        )
                    }
                }
                is ProcessingResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSyncingTodos = false,
                            syncTodosError = result.error.message
                        )
                    }
                }
                is ProcessingResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                syncError = null,
                analysisError = null,
                syncTodosError = null,
                lastSyncMessage = null,
                lastAnalysisMessage = null,
                lastSyncTodosMessage = null
            )
        }
    }
}

data class EmailManagementUiState(
    val isSyncing: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isSyncingTodos: Boolean = false,
    val selectedCount: Int = 0,
    val syncError: String? = null,
    val analysisError: String? = null,
    val syncTodosError: String? = null,
    val lastSyncMessage: String? = null,
    val lastAnalysisMessage: String? = null,
    val lastSyncTodosMessage: String? = null
)
