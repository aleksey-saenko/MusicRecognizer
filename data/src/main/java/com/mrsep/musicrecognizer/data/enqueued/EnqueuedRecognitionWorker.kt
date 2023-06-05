package com.mrsep.musicrecognizer.data.enqueued

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
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.RemoteRecognitionResultType
import com.mrsep.musicrecognizer.data.preferences.PreferencesDataRepository
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.remote.audd.rest.RecognitionDataService
import com.mrsep.musicrecognizer.data.track.TrackDataRepository
import java.time.Instant

@HiltWorker
class EnqueuedRecognitionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackRepository: TrackDataRepository,
    private val preferencesRepository: PreferencesDataRepository,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionDataRepository,
    private val recognitionService: RecognitionDataService,
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
            val userPreferences = preferencesRepository.userPreferencesFlow.first()

            val result = recognitionService.recognize(
                token = userPreferences.apiToken,
                requiredServices = userPreferences.requiredServices,
                file = enqueued.recordFile
            )
            when (result) {
                is RemoteRecognitionDataResult.Success -> {
                    val updatedTrack = trackRepository.insertOrReplaceSaveMetadata(result.data)[0]
                    val updatedResult = result.copy(data = updatedTrack)
                    updateEnqueuedWithResult(enqueued, updatedResult)
                    Result.success()
                }

                is RemoteRecognitionDataResult.NoMatches -> {
                    updateEnqueuedWithResult(enqueued, result)
                    Result.success()
                }

                is RemoteRecognitionDataResult.Error.WrongToken,
                is RemoteRecognitionDataResult.Error.BadRecording -> {
                    updateEnqueuedWithResult(enqueued, result)
                    Result.failure()
                }

                RemoteRecognitionDataResult.Error.BadConnection,
                is RemoteRecognitionDataResult.Error.HttpError,
                is RemoteRecognitionDataResult.Error.UnhandledError -> {
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

    private suspend fun updateEnqueuedWithResult(
        enqueued: EnqueuedRecognitionEntity,
        remoteResult: RemoteRecognitionDataResult
    ) {
        val currentDate = Instant.now()
        val updatedEnqueued = when (remoteResult) {
            is RemoteRecognitionDataResult.Success -> enqueued.copy(
                resultType = RemoteRecognitionResultType.Success,
                resultMbId = remoteResult.data.mbId,
                resultMessage = null,
                resultDate = currentDate
            )

            RemoteRecognitionDataResult.NoMatches -> enqueued.copy(
                resultType = RemoteRecognitionResultType.NoMatches,
                resultMbId = null,
                resultMessage = null,
                resultDate = null
            )

            RemoteRecognitionDataResult.Error.BadConnection -> enqueued.copy(
                resultType = RemoteRecognitionResultType.BadConnection,
                resultMbId = null,
                resultMessage = null,
                resultDate = null
            )

            is RemoteRecognitionDataResult.Error.WrongToken -> enqueued.copy(
                resultType = if (remoteResult.isLimitReached)
                    RemoteRecognitionResultType.LimitedToken
                else
                    RemoteRecognitionResultType.WrongToken,
                resultMbId = null,
                resultMessage = null,
                resultDate = null
            )

            is RemoteRecognitionDataResult.Error.BadRecording -> enqueued.copy(
                resultType = RemoteRecognitionResultType.BadRecording,
                resultMbId = null,
                resultMessage = remoteResult.message,
                resultDate = null
            )

            is RemoteRecognitionDataResult.Error.HttpError ->
                enqueued.copy(
                    resultType = RemoteRecognitionResultType.HttpError,
                    resultMbId = null,
                    resultMessage = "${remoteResult.code}\n${remoteResult.message}",
                    resultDate = null
                )

            is RemoteRecognitionDataResult.Error.UnhandledError ->
                enqueued.copy(
                    resultType = RemoteRecognitionResultType.UnhandledError,
                    resultMbId = null,
                    resultMessage = remoteResult.message,
                    resultDate = null
                )
        }

        enqueuedRecognitionRepository.update(updatedEnqueued)
    }


    companion object {
        const val TAG = "EnqueuedRecognitionWorker"
        private const val MAX_ATTEMPTS = 3
        private const val INPUT_KEY_ENQUEUED_RECOGNITION_ID = "ENQUEUED_RECOGNITION_ID"

        fun getOneTimeWorkRequest(enqueuedRecognitionId: Int): OneTimeWorkRequest {
            val data = Data.Builder()
                .putInt(INPUT_KEY_ENQUEUED_RECOGNITION_ID, enqueuedRecognitionId)
                .build()
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<EnqueuedRecognitionWorker>()
                .addTag(TAG)
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

