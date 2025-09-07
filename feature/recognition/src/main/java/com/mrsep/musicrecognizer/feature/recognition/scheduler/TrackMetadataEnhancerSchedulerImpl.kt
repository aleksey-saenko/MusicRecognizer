package com.mrsep.musicrecognizer.feature.recognition.scheduler

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancerScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class TrackMetadataEnhancerSchedulerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : TrackMetadataEnhancerScheduler {

    private val workManager get() = WorkManager.getInstance(appContext)

    override fun enqueue(trackId: String) {
        workManager.enqueueUniqueWork(
            getUniqueWorkerName(trackId),
            ExistingWorkPolicy.REPLACE,
            TrackMetadataEnhancerWorker.getOneTimeWorkRequest(trackId = trackId)
        )
    }

    override fun isRunning(trackId: String): Flow<Boolean> {
        return workManager.getWorkInfosForUniqueWorkFlow(getUniqueWorkerName(trackId))
            .map { listWorkInfo -> listWorkInfo.lastOrNull()?.state == WorkInfo.State.RUNNING }
            .conflate()
    }

    override fun cancel(trackId: String) {
        workManager.cancelUniqueWork(getUniqueWorkerName(trackId))
    }

    override fun cancelAll() {
        workManager.cancelAllWorkByTag(TrackMetadataEnhancerWorker.TAG)
    }

    private fun getUniqueWorkerName(trackId: String) = UNIQUE_NAME_MASK.plus(trackId)

    companion object {
        private const val UNIQUE_NAME_MASK = "METADATA_ENHANCER_ID#"
    }
}
