package com.mrsep.musicrecognizer.data.enqueued

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatusWithId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EnqueuedRecognitionWorkManagerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : EnqueuedRecognitionWorkDataManager {
    private val workManager get() = WorkManager.getInstance(appContext)

    private fun getUniqueWorkName(enqueuedId: Int) = UNIQUE_NAME_MASK.plus(enqueuedId)

    override fun enqueueRecognitionWorkers(vararg enqueuedId: Int) {
        enqueuedId.forEach { id ->
            workManager.enqueueUniqueWork(
                getUniqueWorkName(id),
                ExistingWorkPolicy.REPLACE,
                EnqueuedRecognitionWorker.getOneTimeWorkRequest(id)
            )
        }
    }

    override fun cancelRecognitionWorkers(vararg enqueuedId: Int) {
        enqueuedId.forEach { id ->
            workManager.cancelUniqueWork(getUniqueWorkName(id))
        }
    }

    override fun cancelAllRecognitionWorkers() {
        workManager.cancelAllWorkByTag(EnqueuedRecognitionWorker.TAG)
    }

    override fun getUniqueWorkInfoFlow(enqueuedId: Int): Flow<EnqueuedRecognitionDataStatus> {
        return workManager.getWorkInfosForUniqueWorkLiveData(getUniqueWorkName(enqueuedId))
            .map { listWorkInfo ->
                if (listWorkInfo.isEmpty()) return@map EnqueuedRecognitionDataStatus.Inactive
                EnqueuedRecognitionWorker.getWorkerStatus(listWorkInfo.lastOrNull())
            }
            .asFlow()
    }

    override fun getAllWorkInfoFlow(): Flow<List<EnqueuedRecognitionDataStatusWithId>> {
        return workManager.getWorkInfosByTagLiveData(EnqueuedRecognitionWorker.TAG).asFlow()
            .filterNotNull()
            .map { listWorkInfo ->
                if (listWorkInfo.isEmpty()) return@map emptyList<EnqueuedRecognitionDataStatusWithId>()
                listWorkInfo.map { workInfo ->
                    // any exceptions here mean the bad request has been created
                    val enqueuedId = workInfo.tags
                        .first { tag -> tag.startsWith(UNIQUE_NAME_MASK) }
                        .substringAfter(UNIQUE_NAME_MASK)
                        .toInt()
                    val status = EnqueuedRecognitionWorker.getWorkerStatus(listWorkInfo.lastOrNull())
                    EnqueuedRecognitionDataStatusWithId(
                        enqueuedId = enqueuedId,
                        status = status
                    )
                }
            }
    }

    companion object {
        private const val UNIQUE_NAME_MASK = "ER_ID#"
    }

}