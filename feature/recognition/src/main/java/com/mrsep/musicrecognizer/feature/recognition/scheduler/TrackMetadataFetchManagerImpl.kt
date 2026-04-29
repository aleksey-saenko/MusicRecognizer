package com.mrsep.musicrecognizer.feature.recognition.scheduler

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataFetchManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class TrackMetadataFetchManagerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : TrackMetadataFetchManager {

    private val workManager get() = WorkManager.getInstance(appContext)

    override fun enqueueTrackLinksFetcher(trackId: String) {
        workManager.enqueueUniqueWork(
            TrackLinksFetchWorker.buildUniqueWorkerName(trackId),
            ExistingWorkPolicy.REPLACE,
            TrackLinksFetchWorker.buildOneTimeWorkRequest(trackId = trackId)
        )
    }

    override fun enqueueLyricsFetcher(trackId: String) {
        workManager.enqueueUniqueWork(
            LyricsFetchWorker.buildUniqueWorkerName(trackId),
            ExistingWorkPolicy.REPLACE,
            LyricsFetchWorker.buildOneTimeWorkRequest(trackId = trackId)
        )
    }

    override fun isTrackLinksFetcherRunning(trackId: String): Flow<Boolean> {
        return isWorkerRunning(TrackLinksFetchWorker.buildUniqueWorkerName(trackId))
    }

    override fun isLyricsFetcherRunning(trackId: String): Flow<Boolean> {
        return isWorkerRunning(LyricsFetchWorker.buildUniqueWorkerName(trackId))
    }

    private fun isWorkerRunning(workerId: String): Flow<Boolean> {
        return workManager.getWorkInfosForUniqueWorkFlow(workerId)
            .map { listWorkInfo -> listWorkInfo.lastOrNull()?.state == WorkInfo.State.RUNNING }
            .conflate()
    }

    override fun cancelTrackLinksFetcher(trackId: String) {
        workManager.cancelUniqueWork(TrackLinksFetchWorker.buildUniqueWorkerName(trackId))
    }

    override fun cancelLyricsFetcher(trackId: String) {
        workManager.cancelUniqueWork(LyricsFetchWorker.buildUniqueWorkerName(trackId))
    }

    override fun cancelAllForTrack(trackId: String) {
        workManager.cancelAllWorkByTag(buildWorkTagForTrack(trackId))
    }

    override fun cancelAll() {
        workManager.cancelAllWorkByTag(TrackLinksFetchWorker.TAG)
        workManager.cancelAllWorkByTag(LyricsFetchWorker.TAG)
    }

    companion object {
        fun buildWorkTagForTrack(trackId: String) = "work_of_track_$trackId"
    }
}
