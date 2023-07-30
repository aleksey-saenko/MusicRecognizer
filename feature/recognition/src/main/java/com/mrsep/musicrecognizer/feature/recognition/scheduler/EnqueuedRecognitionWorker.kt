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
import java.util.concurrent.TimeUnit
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import java.time.Instant

@HiltWorker
internal class EnqueuedRecognitionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val remoteRecognitionService: RemoteRecognitionService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "$TAG started with attempt #$runAttemptCount")
        val enqueuedRecognitionId = inputData.getInt(INPUT_KEY_ENQUEUED_RECOGNITION_ID, -1)
        check(enqueuedRecognitionId != -1) { "$TAG require enqueued recognition id as parameter" }

        return withContext(ioDispatcher) {
            val enqueued = enqueuedRecognitionRepository.getById(enqueuedRecognitionId) ?: run {
                val message = "$TAG finished with fatal error, " +
                        "enqueuedRecognition with id=$id was not found"
                Log.w(TAG, message)
                return@withContext Result.failure()
            }
            if (enqueued.result != null) {
                clearPreviousResult(enqueued)
            }
            val userPreferences = preferencesRepository.userPreferencesFlow.first()

            val result = remoteRecognitionService.recognize(
                token = userPreferences.apiToken,
                requiredServices = userPreferences.requiredServices,
                file = enqueued.recordFile
            )
            when (result) {
                is RemoteRecognitionResult.Success -> {
                    val updatedTrack = trackRepository.insertOrReplaceSaveMetadata(result.track)[0]
                    val updatedResult = result.copy(track = updatedTrack)
                    updateEnqueuedWithResult(enqueued, updatedResult)
                    Result.success()
                }

                is RemoteRecognitionResult.NoMatches -> {
                    updateEnqueuedWithResult(enqueued, result)
                    Result.success()
                }

                is RemoteRecognitionResult.Error.WrongToken,
                is RemoteRecognitionResult.Error.BadRecording -> {
                    updateEnqueuedWithResult(enqueued, result)
                    Result.failure()
                }

                RemoteRecognitionResult.Error.BadConnection,
                is RemoteRecognitionResult.Error.HttpError,
                is RemoteRecognitionResult.Error.UnhandledError -> {
                    if (runAttemptCount >= MAX_ATTEMPTS) {
                        Log.w(TAG, "$TAG canceled, runAttemptCount > max=$MAX_ATTEMPTS")
                        updateEnqueuedWithResult(enqueued, result)
                        Result.failure()
                    } else {
                        Result.retry()
                    }
                }
            }
        }

    }

    private suspend fun clearPreviousResult(enqueued: EnqueuedRecognition) {
        enqueuedRecognitionRepository.update(
            enqueued.copy(result = null, resultDate = null)
        )
    }

    private suspend fun updateEnqueuedWithResult(
        enqueued: EnqueuedRecognition,
        remoteResult: RemoteRecognitionResult
    ) {
        enqueuedRecognitionRepository.update(
            enqueued.copy(result = remoteResult, resultDate = Instant.now())
        )
    }


    companion object {
        const val TAG = "EnqueuedRecognitionWorker"
        private const val MAX_ATTEMPTS = 3
        private const val INPUT_KEY_ENQUEUED_RECOGNITION_ID = "ENQUEUED_RECOGNITION_ID"

        fun getOneTimeWorkRequest(enqueuedId: Int, identifyTag: String): OneTimeWorkRequest {
            val data = Data.Builder()
                .putInt(INPUT_KEY_ENQUEUED_RECOGNITION_ID, enqueuedId)
                .build()
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<EnqueuedRecognitionWorker>()
                .addTag(TAG)
                .addTag(identifyTag)
                .setConstraints(constraints)
                .setInputData(data)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }

    }

}