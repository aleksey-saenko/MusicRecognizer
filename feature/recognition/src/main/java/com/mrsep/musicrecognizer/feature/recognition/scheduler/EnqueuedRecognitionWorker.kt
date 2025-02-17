package com.mrsep.musicrecognizer.feature.recognition.scheduler

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionServiceFactory
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.service.ScheduledResultNotificationHelper
import com.mrsep.musicrecognizer.feature.recognition.service.ext.downloadImageToDiskCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import java.time.Instant

@HiltWorker
internal class EnqueuedRecognitionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recognitionServiceFactory: RecognitionServiceFactory,
    private val trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val scheduledResultNotificationHelper: ScheduledResultNotificationHelper,
    private val trackMetadataEnhancerScheduler: TrackMetadataEnhancerScheduler,
) : CoroutineWorker(appContext, workerParams) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun doWork(): Result {
        Log.d(TAG, "$TAG started with attempt #$runAttemptCount")
        val forceLaunch = inputData.getBoolean(INPUT_KEY_FORCE_LAUNCH, true)
        val recognitionId = inputData.getInt(INPUT_KEY_ENQUEUED_RECOGNITION_ID, -1)
        check(recognitionId != -1) { "$TAG requires enqueued recognition id as parameter" }

        return enqueuedRecognitionRepository
            .getRecognitionFlow(recognitionId)
            .distinctUntilChangedBy { recognition -> recognition?.id }
            .mapLatest { enqueuedRecognition ->
                // Null means that recognition was not found or was deleted in process
                if (enqueuedRecognition == null) {
                    return@mapLatest Result.failure()
                }
                clearPreviousResult(enqueuedRecognition)
                val userPreferences = preferencesRepository.userPreferencesFlow.first()

                val serviceConfig = when (userPreferences.currentRecognitionProvider) {
                    RecognitionProvider.Audd -> userPreferences.auddConfig
                    RecognitionProvider.AcrCloud -> userPreferences.acrCloudConfig
                }
                val recognitionService = recognitionServiceFactory.getService(serviceConfig)
                val result = recognitionService.recognize(enqueuedRecognition.recordFile)

                suspend fun handleRetryOnAttempt(): Result {
                    return if (forceLaunch || runAttemptCount >= MAX_ATTEMPTS) {
                        val log = "$TAG canceled, forceLaunch=$forceLaunch, " +
                                "attempt=$runAttemptCount, maxAttempts=$MAX_ATTEMPTS"
                        Log.w(TAG, log)
                        enqueuedRecognitionRepository.update(
                            enqueuedRecognition.copy(result = result, resultDate = Instant.now())
                        )
                        Result.failure()
                    } else {
                        Result.retry()
                    }
                }
                when (result) {
                    is RemoteRecognitionResult.Success -> {
                        coroutineScope {
                            listOfNotNull(
                                result.track.artworkThumbUrl,
                                result.track.artworkUrl,
                            ).map { imageUrl ->
                                async { appContext.downloadImageToDiskCache(imageUrl) }
                            }.awaitAll()
                        }
                        val trackWithStoredProps = trackRepository
                            .upsertKeepProperties(listOf(result.track))
                            .first()
                        trackRepository.setViewed(trackWithStoredProps.id, false)
                        val updatedTrack = trackWithStoredProps.copy(
                            properties = trackWithStoredProps.properties.copy(isViewed = false)
                        )
                        val updatedResult = result.copy(track = updatedTrack)
                        val updatedEnqueued = enqueuedRecognition.copy(
                            result = updatedResult,
                            resultDate = Instant.now()
                        )
                        enqueuedRecognitionRepository.update(updatedEnqueued)
                        trackMetadataEnhancerScheduler.enqueue(updatedTrack.id)
                        scheduledResultNotificationHelper.notify(updatedEnqueued)
                        Result.success()
                    }

                    is RemoteRecognitionResult.NoMatches -> {
                        enqueuedRecognitionRepository.update(
                            enqueuedRecognition.copy(result = result, resultDate = Instant.now())
                        )
                        Result.success()
                    }

                    RemoteRecognitionResult.Error.BadConnection -> handleRetryOnAttempt()

                    is RemoteRecognitionResult.Error.HttpError -> {
                        if (result.code in 400..499) {
                            enqueuedRecognitionRepository.update(
                                enqueuedRecognition.copy(result = result, resultDate = Instant.now())
                            )
                            Result.failure()
                        } else {
                            handleRetryOnAttempt()
                        }
                    }

                    is RemoteRecognitionResult.Error.AuthError,
                    is RemoteRecognitionResult.Error.ApiUsageLimited,
                    is RemoteRecognitionResult.Error.BadRecording,
                    is RemoteRecognitionResult.Error.UnhandledError,
                    -> {
                        enqueuedRecognitionRepository.update(
                            enqueuedRecognition.copy(result = result, resultDate = Instant.now())
                        )
                        Result.failure()
                    }
                }
            }
            .first()
    }

    private suspend fun clearPreviousResult(enqueued: EnqueuedRecognition) {
        if (enqueued.result != null) {
            enqueuedRecognitionRepository.update(
                enqueued.copy(result = null, resultDate = null)
            )
        }
    }

    companion object {
        const val TAG = "EnqueuedRecognitionWorker"
        private const val MAX_ATTEMPTS = 3
        private const val INPUT_KEY_ENQUEUED_RECOGNITION_ID = "ENQUEUED_RECOGNITION_ID"
        private const val INPUT_KEY_FORCE_LAUNCH = "FORCE_LAUNCH"

        fun getOneTimeWorkRequest(
            recognitionId: Int,
            identifyTag: String,
            forceLaunch: Boolean,
        ): OneTimeWorkRequest {
            val data = Data.Builder()
                .putInt(INPUT_KEY_ENQUEUED_RECOGNITION_ID, recognitionId)
                .putBoolean(INPUT_KEY_FORCE_LAUNCH, forceLaunch)
                .build()
            val constraints = if (forceLaunch) {
                Constraints.NONE
            } else {
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            }
            return OneTimeWorkRequestBuilder<EnqueuedRecognitionWorker>()
                .addTag(TAG)
                .addTag(identifyTag)
                .setConstraints(constraints)
                .setInputData(data)
                .build()
        }
    }
}
