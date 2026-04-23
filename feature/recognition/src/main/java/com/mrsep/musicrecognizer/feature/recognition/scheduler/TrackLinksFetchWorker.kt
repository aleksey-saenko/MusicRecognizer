package com.mrsep.musicrecognizer.feature.recognition.scheduler

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
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.scheduler.TrackMetadataFetchManagerImpl.Companion.workTagForTrack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first

@HiltWorker
internal class TrackLinksFetchWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository,
) : CoroutineWorker(appContext, workerParams) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun doWork(): Result {
        Log.d(TAG, "$TAG started with attempt #$runAttemptCount")
        val trackId = inputData.getString(INPUT_KEY_TRACK_ID)
        checkNotNull(trackId) { "$TAG requires track ID as parameter" }

        val requiredServices = preferencesRepository.userPreferencesFlow.first()
            .requiredMusicServices.toSet()
        if (requiredServices.isEmpty()) return Result.success()
        return when (val result = trackRepository.fetchAndUpdateTrackLinks(trackId, requiredServices)) {
            is NetworkResult.Success -> Result.success()
            is NetworkError.BadConnection -> handleRetryOnAttempt()
            is NetworkError.HttpError -> {
                if (result.isClientError) Result.failure() else handleRetryOnAttempt()
            }
            is NetworkError.UnhandledError -> Result.failure()
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
        const val TAG = "TrackLinksFetchWorker"
        private const val MAX_ATTEMPTS = 3
        private const val INPUT_KEY_TRACK_ID = "TRACK_ID"

        fun getUniqueWorkerName(trackId: String) = "TRACK_LINKS_FETCHER_ID#$trackId"

        fun getOneTimeWorkRequest(trackId: String): OneTimeWorkRequest {
            val data = Data.Builder()
                .putString(INPUT_KEY_TRACK_ID, trackId)
                .build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<TrackLinksFetchWorker>()
                .addTag(TAG)
                .addTag(workTagForTrack(trackId))
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
