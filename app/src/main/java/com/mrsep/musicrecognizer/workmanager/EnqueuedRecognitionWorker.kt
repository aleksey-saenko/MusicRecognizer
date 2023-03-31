package com.mrsep.musicrecognizer.workmanager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.*
import com.mrsep.musicrecognizer.domain.model.RemoteRecognitionResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognitionWorkerStatus

@HiltWorker
class EnqueuedRecognitionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val recognitionService: RecognitionService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "$TAG started with attempt #$runAttemptCount")
        val enqueuedRecognitionId = inputData.getInt(INPUT_KEY_ENQUEUED_RECOGNITION_ID, -1)
        check(enqueuedRecognitionId != -1) { "$TAG require enqueued recognition id as parameter" }

        return withContext(ioDispatcher) {
            val enqueued = enqueuedRecognitionRepository.getById(enqueuedRecognitionId) ?: run {
                Log.w(TAG, "$TAG finished with fatal error, enqueuedRecognition id=$id not found")
                return@withContext Result.failure(
                    getFailureData(appContext.getString(R.string.illegal_state))
                )
            }
            val userPreferences = preferencesRepository.userPreferencesFlow.first()

            when (val result = recognitionService.recognize(
                token = userPreferences.apiToken,
                requiredServices = userPreferences.requiredServices,
                file = enqueued.recordFile
            )) {
                is RemoteRecognitionResult.Success -> {
                    val newTrack = trackRepository.insertOrReplaceSaveMetadata(result.data)[0]
                    Result.success(getSuccessData(newTrack.mbId))
                }
                is RemoteRecognitionResult.NoMatches -> {
                    Result.success(getSuccessData(""))
                }
                is RemoteRecognitionResult.Error -> {
                    val message = when (result) {
                        RemoteRecognitionResult.Error.BadConnection ->
                            appContext.getString(R.string.no_internet_connection)
                        is RemoteRecognitionResult.Error.WrongToken ->
                            appContext.getString(R.string.invalid_token)
                        else -> appContext.getString(R.string.unknown_error)
                    }
                    if (runAttemptCount >= MAX_ATTEMPTS) {
                        Log.w(TAG, "$TAG canceled, runAttemptCount > max=$MAX_ATTEMPTS")
                        Result.failure(getFailureData(message))
                    } else {
                        Result.retry()
                    }
                }
            }
        }

    }

    private fun getFailureData(message: String) = workDataOf(OUTPUT_KEY_FAILURE_MESSAGE to message)
    private fun getSuccessData(trackMbId: String) = workDataOf(OUTPUT_KEY_TRACK_MB_ID to trackMbId)

    companion object {
        const val TAG = "EnqueuedRecognitionWorker"
        private const val MAX_ATTEMPTS = 3
        private const val INPUT_KEY_ENQUEUED_RECOGNITION_ID = "ENQUEUED_RECOGNITION_ID"

        private const val OUTPUT_KEY_TRACK_MB_ID = "TRACK_MB_ID"
        private const val OUTPUT_KEY_FAILURE_MESSAGE = "FAILURE_MESSAGE"

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

        fun getWorkerStatus(workInfo: WorkInfo?): EnqueuedRecognitionWorkerStatus {
            return workInfo?.let {
                when (it.state) {
                    WorkInfo.State.ENQUEUED -> EnqueuedRecognitionWorkerStatus.Enqueued
                    WorkInfo.State.RUNNING -> EnqueuedRecognitionWorkerStatus.Running
                    WorkInfo.State.CANCELLED -> EnqueuedRecognitionWorkerStatus.Canceled
                    WorkInfo.State.SUCCEEDED -> {
                        val trackMbId = checkNotNull(
                            workInfo.outputData.getString(OUTPUT_KEY_TRACK_MB_ID)
                        )
                        if (trackMbId.isEmpty()) {
                            EnqueuedRecognitionWorkerStatus.Finished.NotFound
                        } else {
                            EnqueuedRecognitionWorkerStatus.Finished.Success(trackMbId)
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        val message =
                            workInfo.outputData.getString(OUTPUT_KEY_FAILURE_MESSAGE) ?: ""
                        EnqueuedRecognitionWorkerStatus.Finished.Error(message)
                    }
                    WorkInfo.State.BLOCKED -> throw IllegalStateException(
                        "EnqueuedRecognitionWorkerStatus doesn't support chain of workers"
                    )
                }
            } ?: EnqueuedRecognitionWorkerStatus.Inactive
        }
    }

}

