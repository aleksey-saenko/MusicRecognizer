package com.mrsep.musicrecognizer.feature.recognition.scheduler

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduledJobStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class EnqueuedRecognitionSchedulerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : EnqueuedRecognitionScheduler {

    private val workManager get() = WorkManager.getInstance(appContext)

    // Use uniqueWorkName as an extra tag to associate workInfo with recognitionId
    // in the getStatusFlowAll method.
    // WorkInfo only contains the worker UUID (which cannot be converted in both directions) and tags.
    override fun enqueue(recognitionIds: List<Int>, forceLaunch: Boolean) {
        recognitionIds.forEach { id ->
            val uniqueWorkName = getUniqueWorkerName(id)
            workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                EnqueuedRecognitionWorker.getOneTimeWorkRequest(
                    recognitionId = id,
                    identifyTag = uniqueWorkName,
                    forceLaunch = forceLaunch
                )
            )
        }
    }

    override fun cancel(recognitionIds: List<Int>) {
        recognitionIds.forEach { id ->
            workManager.cancelUniqueWork(getUniqueWorkerName(id))
        }
    }

    override fun cancelAll() {
        workManager.cancelAllWorkByTag(EnqueuedRecognitionWorker.TAG)
    }

    override fun getJobStatusFlow(recognitionId: Int): Flow<ScheduledJobStatus> {
        return workManager.getWorkInfosForUniqueWorkFlow(getUniqueWorkerName(recognitionId))
            .map { listWorkInfo -> listWorkInfo.lastOrNull().asScheduledJobStatus() }
            .conflate()
    }

    override fun getJobStatusForAllFlow(): Flow<Map<Int, ScheduledJobStatus>> {
        return workManager.getWorkInfosByTagFlow(EnqueuedRecognitionWorker.TAG)
            .map { listWorkInfo ->
                listWorkInfo.mapNotNull { workInfo ->
                    val recognitionId = workInfo.tags.find { tag -> tag.startsWith(UNIQUE_NAME_MASK) }
                        ?.substring(UNIQUE_NAME_MASK.length)?.toIntOrNull()
                    recognitionId?.let { recognitionId to workInfo.asScheduledJobStatus() }
                }.toMap()
            }
            .conflate()
    }

    private fun getUniqueWorkerName(recognitionId: Int) = UNIQUE_NAME_MASK.plus(recognitionId)

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

            WorkInfo.State.BLOCKED -> {
                error("EnqueuedRecognitionWorker implemented without a chain of workers")
            }
        }
    } ?: ScheduledJobStatus.INACTIVE
}
