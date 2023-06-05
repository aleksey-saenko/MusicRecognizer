package com.mrsep.musicrecognizer.data.enqueued

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EnqueuedRecognitionWorkManagerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : EnqueuedRecognitionWorkDataManager {
    private val workManager get() = WorkManager.getInstance(appContext)

    private fun getUniqueWorkerName(enqueuedId: Int) = UNIQUE_NAME_MASK.plus(enqueuedId)

    override fun enqueueWorkers(vararg enqueuedId: Int) {
        enqueuedId.forEach { id ->
            workManager.enqueueUniqueWork(
                getUniqueWorkerName(id),
                ExistingWorkPolicy.REPLACE,
                EnqueuedRecognitionWorker.getOneTimeWorkRequest(id)
            )
        }
    }

    override fun cancelWorkers(vararg enqueuedId: Int) {
        enqueuedId.forEach { id ->
            workManager.cancelUniqueWork(getUniqueWorkerName(id))
        }
    }

    override fun cancelWorkersAll() {
        workManager.cancelAllWorkByTag(EnqueuedRecognitionWorker.TAG)
    }

    override fun getWorkInfoFlowById(enqueuedId: Int): Flow<EnqueuedRecognitionDataStatus> {
        return workManager.getWorkInfosForUniqueWorkLiveData(getUniqueWorkerName(enqueuedId))
            .map { listWorkInfo -> listWorkInfo.lastOrNull().toEnqueuedStatus() }
            .asFlow()
    }

    companion object {
        private const val UNIQUE_NAME_MASK = "ER_ID#"
    }

}

private fun WorkInfo?.toEnqueuedStatus(): EnqueuedRecognitionDataStatus {
    return this?.let { workInfo ->
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> EnqueuedRecognitionDataStatus.Enqueued
            WorkInfo.State.RUNNING -> EnqueuedRecognitionDataStatus.Running
            WorkInfo.State.CANCELLED,
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.FAILED -> EnqueuedRecognitionDataStatus.Inactive

            WorkInfo.State.BLOCKED -> throw IllegalStateException(
                "EnqueuedRecognitionWorker implemented without a chain of workers"
            )
        }
    } ?: EnqueuedRecognitionDataStatus.Inactive
}