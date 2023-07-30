package com.mrsep.musicrecognizer.feature.recognition.scheduler

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduledJobStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject

internal class EnqueuedRecognitionSchedulerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : EnqueuedRecognitionScheduler {

    private val workManager get() = WorkManager.getInstance(appContext)

    // Use uniqueWorkName as an extra tag to associate workInfo with enqueuedId
    // in the getStatusFlowAll method.
    // WorkInfo only contains the worker UUID (which cannot be converted in both directions) and tags.
    override fun enqueueById(vararg enqueuedId: Int) {
        enqueuedId.forEach { id ->
            val uniqueWorkName = getUniqueWorkerName(id)
            workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                EnqueuedRecognitionWorker.getOneTimeWorkRequest(
                    enqueuedId = id,
                    identifyTag = uniqueWorkName
                )
            )
        }
    }

    override fun cancelById(vararg enqueuedId: Int) {
        enqueuedId.forEach { id ->
            workManager.cancelUniqueWork(getUniqueWorkerName(id))
        }
    }

    override fun cancelAll() {
        workManager.cancelAllWorkByTag(EnqueuedRecognitionWorker.TAG)
    }

    override fun getStatusFlowById(enqueuedId: Int): Flow<ScheduledJobStatus> {
        return workManager.getWorkInfosForUniqueWorkLiveData(getUniqueWorkerName(enqueuedId))
            .map { listWorkInfo -> listWorkInfo.lastOrNull().asScheduledJobStatus() }
            .asFlow().conflate()
    }

    override fun getStatusFlowAll(): Flow<Map<Int, ScheduledJobStatus>> {
        return workManager.getWorkInfosByTagLiveData(EnqueuedRecognitionWorker.TAG)
            .map { listWorkInfo ->
                listWorkInfo.mapNotNull { workInfo ->
                    val enqueuedId = workInfo.tags.find { tag -> tag.startsWith(UNIQUE_NAME_MASK) }
                        ?.substring(UNIQUE_NAME_MASK.length)?.toIntOrNull()
                    enqueuedId?.let { enqueuedId to workInfo.asScheduledJobStatus() }
                }.toMap()
            }
            .asFlow().conflate()
    }

    private fun getUniqueWorkerName(enqueuedId: Int) = UNIQUE_NAME_MASK.plus(enqueuedId)

    companion object {
        private const val UNIQUE_NAME_MASK = "ER_ID#"
    }

}

private fun WorkInfo?.asScheduledJobStatus(): ScheduledJobStatus {
    return this?.let { workInfo ->
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> ScheduledJobStatus.ENQUEUED
            WorkInfo.State.RUNNING -> ScheduledJobStatus.RUNNING
            WorkInfo.State.CANCELLED,
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.FAILED -> ScheduledJobStatus.INACTIVE

            WorkInfo.State.BLOCKED -> throw IllegalStateException(
                "EnqueuedRecognitionWorker implemented without a chain of workers"
            )
        }
    } ?: ScheduledJobStatus.INACTIVE
}