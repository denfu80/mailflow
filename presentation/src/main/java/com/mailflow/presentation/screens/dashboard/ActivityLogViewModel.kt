package com.mailflow.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mailflow.domain.repository.ProcessingLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityLogUiState(
    val logs: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    private val processingLogRepository: ProcessingLogRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<ActivityLogUiState> = processingLogRepository.getRecentLogs()
        .map { logs ->
            ActivityLogUiState(
                logs = logs.map { it.toDisplayString() },
                isLoading = _isRefreshing.value,
                error = null
            )
        }
        .catch { e ->
            emit(ActivityLogUiState(
                logs = emptyList(),
                isLoading = false,
                error = e.message ?: "Unknown error"
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityLogUiState(isLoading = true)
        )

    fun onRefresh() {
        // The Flow automatically updates, so we just need to mark as refreshing
        _isRefreshing.value = true
        // Reset after a short delay (the Flow will update automatically)
        viewModelScope.launch {
            delay(500)
            _isRefreshing.value = false
        }
    }
}

