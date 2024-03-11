package com.mrsep.musicrecognizer.feature.recognition.platform

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackMetadataEnhancer
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteMetadataEnhancingResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
internal class TrackMetadataEnhancerWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackRepository: TrackRepository,
    private val trackMetadataEnhancer: TrackMetadataEnhancer,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "$TAG started with attempt #$runAttemptCount")
        val trackId = inputData.getString(INPUT_KEY_TRACK_ID)
        checkNotNull(trackId) { "$TAG requires track ID as parameter" }

        return withContext(ioDispatcher) {
            val oldTrack = trackRepository.getTrack(trackId)
                ?: return@withContext Result.failure()
            when (val result = trackMetadataEnhancer.enhance(oldTrack)) {
                is RemoteMetadataEnhancingResult.Success -> {
                    trackRepository.updateKeepUserProperties(result.track)
                    Result.success()
                }

                RemoteMetadataEnhancingResult.NoEnhancement -> Result.success()

                RemoteMetadataEnhancingResult.Error.BadConnection -> handleRetryOnAttempt()

                is RemoteMetadataEnhancingResult.Error.HttpError -> {
                    if (result.code in 400..499) {
                        val log = "$TAG canceled, remote result code=${result.code}, " +
                                "message=${result.message}"
                        Log.w(TAG, log)
                        Result.failure()
                    } else {
                        handleRetryOnAttempt()
                    }
                }

                is RemoteMetadataEnhancingResult.Error.UnhandledError -> Result.failure()
            }
        }
    }

    private fun handleRetryOnAttempt(): Result {
        return if (runAttemptCount >= MAX_ATTEMPTS) {
            Log.w(TAG, "$TAG canceled, attempt=$runAttemptCount, maxAttempts=$MAX_ATTEMPTS")
            Result.failure()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val TAG = "TrackMetadataEnhancerWorker"
        private const val MAX_ATTEMPTS = 3
        private const val INPUT_KEY_TRACK_ID = "TRACK_ID"

        fun getOneTimeWorkRequest(trackId: String): OneTimeWorkRequest {
            val data = Data.Builder()
                .putString(INPUT_KEY_TRACK_ID, trackId)
                .build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<TrackMetadataEnhancerWorker>()
                .addTag(TAG)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    }
                }
                .setConstraints(constraints)
                .setInputData(data)
                .build()
        }

    }

}