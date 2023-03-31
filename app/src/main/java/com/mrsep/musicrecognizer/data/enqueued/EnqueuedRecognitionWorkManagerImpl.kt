package com.mrsep.musicrecognizer.data.enqueued

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mrsep.musicrecognizer.domain.EnqueuedRecognitionWorkManager
import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.workmanager.EnqueuedRecognitionWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EnqueuedRecognitionWorkManagerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : EnqueuedRecognitionWorkManager {
    private val workManager get() = WorkManager.getInstance(appContext)

    override fun enqueueRecognitionWorker(enqueuedRecognition: EnqueuedRecognition) {
        workManager.enqueueUniqueWork(
            enqueuedRecognition.uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            EnqueuedRecognitionWorker.getOneTimeWorkRequest(enqueuedRecognition.id)
        )
    }

    override fun cancelRecognitionWorker(enqueuedRecognition: EnqueuedRecognition) {
        workManager.cancelUniqueWork(enqueuedRecognition.uniqueWorkName)
    }

    override fun getUniqueWorkInfoFlow(enqueuedRecognition: EnqueuedRecognition): Flow<WorkInfo?> {
        return workManager.getWorkInfosForUniqueWorkLiveData(enqueuedRecognition.uniqueWorkName)
            .map { listWorkInfo ->
                if (listWorkInfo.isEmpty()) return@map null
                listWorkInfo.last()
            }
            .asFlow()
    }

    override fun getAllWorkInfoFlow(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosByTagLiveData(EnqueuedRecognitionWorker.TAG).asFlow()
    }

}