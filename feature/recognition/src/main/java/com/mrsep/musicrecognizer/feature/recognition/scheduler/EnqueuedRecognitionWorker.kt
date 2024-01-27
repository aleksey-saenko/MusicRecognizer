package com.mrsep.musicrecognizer.feature.recognition.scheduler

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionServiceFactory
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionProvider
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.ScheduledResultNotificationHelper
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "$TAG started with attempt #$runAttemptCount")
        val forceLaunch = inputData.getBoolean(INPUT_KEY_FORCE_LAUNCH, true)
        val recognitionId = inputData.getInt(INPUT_KEY_ENQUEUED_RECOGNITION_ID, -1)
        check(recognitionId != -1) { "$TAG requires enqueued recognition id as parameter" }

        return withContext(ioDispatcher) {
            val enqueuedRecognition = enqueuedRecognitionRepository.getRecognition(recognitionId)
            if (enqueuedRecognition == null) {
                val message = "$TAG finished with fatal error, " +
                        "enqueuedRecognition with id=$id was not found"
                Log.w(TAG, message)
                return@withContext Result.failure()
            }
            clearPreviousResult(enqueuedRecognition)
            val userPreferences = preferencesRepository.userPreferencesFlow.first()

            val serviceConfig = when (userPreferences.currentRecognitionProvider) {
                RecognitionProvider.Audd -> userPreferences.auddConfig
                RecognitionProvider.AcrCloud -> userPreferences.acrCloudConfig
            }
            val recognitionService = recognitionServiceFactory.getService(serviceConfig)

            val result = recognitionService.recognize(
                enqueuedRecognition.recordFile
            )
            when (result) {
                is RemoteRecognitionResult.Success -> {
                    val nowDate = Instant.now()
                    val trackWithStoredProps = trackRepository.upsertKeepProperties(result.track)[0]
                    trackRepository.setRecognitionDate(trackWithStoredProps.id, nowDate)
                    val updatedTrack = trackWithStoredProps.copy(
                        properties = trackWithStoredProps.properties.copy(
                            lastRecognitionDate = nowDate
                        )
                    )
                    val updatedResult = result.copy(track = updatedTrack)
                    val updatedEnqueued =
                        enqueuedRecognition.copy(result = updatedResult, resultDate = Instant.now())
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

                is RemoteRecognitionResult.Error.WrongToken,
                is RemoteRecognitionResult.Error.BadRecording -> {
                    enqueuedRecognitionRepository.update(
                        enqueuedRecognition.copy(result = result, resultDate = Instant.now())
                    )
                    Result.failure()
                }

                RemoteRecognitionResult.Error.BadConnection,
                is RemoteRecognitionResult.Error.HttpError,
                is RemoteRecognitionResult.Error.UnhandledError -> {
                    if (forceLaunch || runAttemptCount >= MAX_ATTEMPTS) {
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
            }
        }

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
            forceLaunch: Boolean
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