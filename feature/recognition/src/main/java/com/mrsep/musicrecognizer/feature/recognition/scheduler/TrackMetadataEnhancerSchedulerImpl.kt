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

    override fun enqueueTrackLinksFetcher(trackId: String) {
        workManager.enqueueUniqueWork(
            TrackLinksFetcherWorker.getUniqueWorkerName(trackId),
            ExistingWorkPolicy.REPLACE,
            TrackLinksFetcherWorker.getOneTimeWorkRequest(trackId = trackId)
        )
    }

    override fun enqueueLyricsFetcher(trackId: String) {
        workManager.enqueueUniqueWork(
            LyricsFetchWorker.getUniqueWorkerName(trackId),
            ExistingWorkPolicy.REPLACE,
            LyricsFetchWorker.getOneTimeWorkRequest(trackId = trackId)
        )
    }

    override fun isTrackLinksFetcherRunning(trackId: String): Flow<Boolean> {
        return isWorkerRunning(TrackLinksFetcherWorker.getUniqueWorkerName(trackId))
    }

    override fun isLyricsFetcherRunning(trackId: String): Flow<Boolean> {
        return isWorkerRunning(LyricsFetchWorker.getUniqueWorkerName(trackId))
    }

    private fun isWorkerRunning(workerId: String): Flow<Boolean> {
        return workManager.getWorkInfosForUniqueWorkFlow(workerId)
            .map { listWorkInfo -> listWorkInfo.lastOrNull()?.state == WorkInfo.State.RUNNING }
            .conflate()
    }

    override fun cancelTrackLinksFetcher(trackId: String) {
        workManager.cancelUniqueWork(TrackLinksFetcherWorker.getUniqueWorkerName(trackId))
    }

    override fun cancelLyricsFetcher(trackId: String) {
        workManager.cancelUniqueWork(LyricsFetchWorker.getUniqueWorkerName(trackId))
    }

    override fun cancelAll() {
        workManager.cancelAllWorkByTag(TrackLinksFetcherWorker.TAG)
        workManager.cancelAllWorkByTag(LyricsFetchWorker.TAG)
    }
}
