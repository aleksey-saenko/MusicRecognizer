package com.mrsep.musicrecognizer.feature.recognition.scheduler

import android.content.Context
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.scheduler.TrackMetadataFetchManagerImpl.Companion.buildWorkTagForTrack
import com.mrsep.musicrecognizer.feature.recognition.service.ext.prefetchArtworkAndGenerateSeedColor
import com.mrsep.musicrecognizer.feature.recognition.widget.RecognitionWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class TrackArtworkPrefetchWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackRepository: TrackRepository,
    @WidgetStatusHolder private val widgetStatusHolder: RecognitionStatusHolder,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val trackId = inputData.getString(KEY_TRACK_ID) ?: return Result.failure()
        val artworkUrl = inputData.getString(KEY_ARTWORK_URL) ?: return Result.failure()

        val isSuccess = appContext.prefetchArtworkAndGenerateSeedColor(artworkUrl) { seedColor ->
            trackRepository.setThemeSeedColor(trackId, seedColor)
        }

        if (isSuccess && hasActiveWidgets() && isWidgetStatusForTrack(trackId)) {
            RecognitionWidget().updateAll(appContext)
        }

        return if (isSuccess) Result.success() else Result.failure()
    }

    private suspend fun hasActiveWidgets(): Boolean {
        return GlanceAppWidgetManager(appContext)
            .getGlanceIds(RecognitionWidget::class.java)
            .isNotEmpty()
    }

    private fun isWidgetStatusForTrack(trackId: String): Boolean {
        return when (val status = widgetStatusHolder.status.value) {
            is RecognitionStatus.Done -> {
                (status.result as? RecognitionResult.Success)?.track?.id == trackId
            }
            else -> false
        }
    }

    companion object {
        const val TAG = "TrackArtworkPrefetchWorker"
        private const val KEY_TRACK_ID = "TRACK_ID"
        private const val KEY_ARTWORK_URL = "ARTWORK_URL"

        fun buildUniqueWorkerName(trackId: String): String = "ARTWORK_PREFETCH_TRACK_ID_$trackId"

        // Don't apply network constraints, because the fetched image is required for immediate display
        // If there is no internet, the worker should fail immediately
        fun buildRequest(trackId: String, artworkUrl: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<TrackArtworkPrefetchWorker>()
                .setInputData(
                    workDataOf(
                        KEY_TRACK_ID to trackId,
                        KEY_ARTWORK_URL to artworkUrl,
                    ),
                )
                .addTag(TAG)
                .addTag(buildWorkTagForTrack(trackId))
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    }
                }
                .build()
        }
    }
}
