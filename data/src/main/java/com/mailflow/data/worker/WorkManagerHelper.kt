package com.mailflow.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicGmailSync(
        intervalMinutes: Long = DEFAULT_SYNC_INTERVAL_MINUTES,
        flexMinutes: Long = DEFAULT_FLEX_INTERVAL_MINUTES
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<GmailSyncWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = flexMinutes,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(TAG_SYNC_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            GmailSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    fun scheduleOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<GmailSyncWorker>()
            .setConstraints(constraints)
            .addTag(TAG_SYNC_WORK)
            .build()

        workManager.enqueueUniqueWork(
            "${GmailSyncWorker.WORK_NAME}_onetime",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    fun scheduleEmailProcessing() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val processingWorkRequest = OneTimeWorkRequestBuilder<EmailProcessingWorker>()
            .setConstraints(constraints)
            .addTag(TAG_PROCESSING_WORK)
            .build()

        workManager.enqueueUniqueWork(
            EmailProcessingWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            processingWorkRequest
        )
    }

    fun scheduleSyncWithProcessing() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<GmailSyncWorker>()
            .setConstraints(constraints)
            .addTag(TAG_SYNC_WORK)
            .build()

        val processingWorkRequest = OneTimeWorkRequestBuilder<EmailProcessingWorker>()
            .setConstraints(constraints)
            .addTag(TAG_PROCESSING_WORK)
            .build()

        workManager.beginUniqueWork(
            CHAIN_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
            .then(processingWorkRequest)
            .enqueue()
    }

    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(GmailSyncWorker.WORK_NAME)
    }

    fun cancelAllWork() {
        workManager.cancelAllWorkByTag(TAG_SYNC_WORK)
        workManager.cancelAllWorkByTag(TAG_PROCESSING_WORK)
    }

    fun observeSyncWorkInfo(): Flow<WorkInfo?> {
        return workManager.getWorkInfosForUniqueWorkFlow(GmailSyncWorker.WORK_NAME)
            .map { workInfos -> workInfos.firstOrNull() }
    }

    fun observeAllSyncWork(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosByTagFlow(TAG_SYNC_WORK)
    }

    fun observeAllProcessingWork(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosByTagFlow(TAG_PROCESSING_WORK)
    }

    companion object {
        const val DEFAULT_SYNC_INTERVAL_MINUTES = 30L
        const val DEFAULT_FLEX_INTERVAL_MINUTES = 10L
        const val TAG_SYNC_WORK = "sync_work"
        const val TAG_PROCESSING_WORK = "processing_work"
        const val CHAIN_WORK_NAME = "sync_and_process_chain"
    }
}
