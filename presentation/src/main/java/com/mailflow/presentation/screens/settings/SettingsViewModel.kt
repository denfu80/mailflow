package com.mailflow.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.mailflow.data.worker.WorkManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val workManagerHelper: WorkManagerHelper
) : ViewModel() {

    val syncWorkStatus: StateFlow<WorkStatus> = workManagerHelper.observeSyncWorkInfo()
        .map { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> WorkStatus.Running
                WorkInfo.State.ENQUEUED -> WorkStatus.Scheduled
                WorkInfo.State.SUCCEEDED -> WorkStatus.Succeeded(
                    messagesFetched = workInfo.outputData.getInt(
                        com.mailflow.data.worker.GmailSyncWorker.KEY_MESSAGES_FETCHED,
                        0
                    ),
                    messagesProcessed = workInfo.outputData.getInt(
                        com.mailflow.data.worker.GmailSyncWorker.KEY_MESSAGES_PROCESSED,
                        0
                    )
                )
                WorkInfo.State.FAILED -> WorkStatus.Failed(
                    error = workInfo.outputData.getString(
                        com.mailflow.data.worker.GmailSyncWorker.KEY_ERROR_MESSAGE
                    ) ?: "Unknown error"
                )
                WorkInfo.State.BLOCKED -> WorkStatus.Blocked
                WorkInfo.State.CANCELLED -> WorkStatus.Cancelled
                else -> WorkStatus.Idle
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkStatus.Idle
        )

    val allSyncWork: StateFlow<List<WorkInfo>> = workManagerHelper.observeAllSyncWork()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allProcessingWork: StateFlow<List<WorkInfo>> = workManagerHelper.observeAllProcessingWork()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun startPeriodicSync(intervalMinutes: Long = WorkManagerHelper.DEFAULT_SYNC_INTERVAL_MINUTES) {
        viewModelScope.launch {
            workManagerHelper.schedulePeriodicGmailSync(intervalMinutes)
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            workManagerHelper.scheduleOneTimeSync()
        }
    }

    fun cancelPeriodicSync() {
        viewModelScope.launch {
            workManagerHelper.cancelPeriodicSync()
        }
    }

    fun cancelAllWork() {
        viewModelScope.launch {
            workManagerHelper.cancelAllWork()
        }
    }
}

sealed class WorkStatus {
    data object Idle : WorkStatus()
    data object Scheduled : WorkStatus()
    data object Running : WorkStatus()
    data object Blocked : WorkStatus()
    data object Cancelled : WorkStatus()
    data class Succeeded(val messagesFetched: Int, val messagesProcessed: Int) : WorkStatus()
    data class Failed(val error: String) : WorkStatus()
}
