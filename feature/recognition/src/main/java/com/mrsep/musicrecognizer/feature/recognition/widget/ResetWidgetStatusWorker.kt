package com.mrsep.musicrecognizer.feature.recognition.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.service.ResultNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.selects.select

@HiltWorker
internal class ResetWidgetStatusWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @WidgetStatusHolder private val statusHolder: RecognitionStatusHolder,
    private val trackRepository: TrackRepository,
    private val notificationHelper: ResultNotificationHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val statusSnapshot = statusHolder.status.value
        if (statusSnapshot !is RecognitionStatus.Done) return@coroutineScope Result.success()

        select {
            val result = statusSnapshot.result
            if (result is RecognitionResult.Success) {
                launch {
                    trackRepository.getTrackFlow(result.track.id)
                        .map { track -> track?.properties?.isViewed ?: true }
                        .first { isViewed -> isViewed }
                    notificationHelper.cancelResultNotification()
                }.onJoin { }
            }
            launch { delay(RESULT_RESET_DELAY_MILLIS) }.onJoin { }
        }

        statusHolder.resetFinalStatus()
        RecognitionWidget().updateAll(appContext)
        Result.success()
    }

    companion object {
        private const val TAG = "ResetWidgetStatusWorker"
        private const val RESULT_RESET_DELAY_MILLIS = 25_000L

        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<ResetWidgetStatusWorker>()
                    .addTag(TAG)
                    .build()
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(TAG)
        }
    }
}
