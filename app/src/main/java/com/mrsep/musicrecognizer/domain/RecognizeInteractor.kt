package com.mrsep.musicrecognizer.domain

import android.content.Context
import android.util.Log
import com.mrsep.musicrecognizer.BuildConfig
import com.mrsep.musicrecognizer.di.ApplicationScope
import com.mrsep.musicrecognizer.di.DefaultDispatcher
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.model.RemoteRecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

private const val token = BuildConfig.AUDD_TOKEN

@Singleton
class RecognizeInteractor @Inject constructor(
    @ApplicationContext appContext: Context,
    @ApplicationScope private val appScope: CoroutineScope,
    private val recognizeService: RecognizeService,
    private val trackRepository: TrackRepository,
    private val recorderController: RecorderController,
    private val preferencesRepository: PreferencesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    val maxAmplitudeFlow get() = recorderController.maxAmplitudeFlow

    private val _statusFlow: MutableStateFlow<RecognizeStatus> =
        MutableStateFlow(RecognizeStatus.Ready)
    val statusFlow: StateFlow<RecognizeStatus> get() = _statusFlow

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            statusFlow.collect {
                Log.w("SUPER", it.toString())
            }
        }
    }

    val recordFile =
        File("${appContext.filesDir.absolutePath}/${RECORD_FILE_NAME}")

    private var recognizeJob: Job? = null

    fun launchRecognizeOrCancel(scope: CoroutineScope) {
        if (recognizeJob?.isActive == true) {
            cancelRecognize()
        } else {
            recognizeJob = scope.launch { launchRecognize() }.apply {
                invokeOnCompletion(::jobCancellationHandler)
            }
        }
    }

    private fun jobCancellationHandler(cause: Throwable?) {
        Log.d("jobCompletionHandler", "cause=$cause")
        when (cause) {
            is CancellationException -> {
                recognizeJob = null
                _statusFlow.update { RecognizeStatus.Ready }
            }
        }
    }

    fun cancelRecognize() {
        recognizeJob?.cancel()
    }

    fun resetStatusToReady() {
        _statusFlow.update { RecognizeStatus.Ready }
    }

    private suspend fun launchRecognize() {
        withContext(ioDispatcher) {
            val userPreferences = preferencesRepository.userPreferencesFlow.first()

            _statusFlow.update { RecognizeStatus.Listening }
            val recordResult = recorderController.recordAudioToFile(
                recordFile,
                Duration.ofMillis(RECORD_DURATION_IN_MILLIS)
            )
            Log.w("SUPER", recordResult.toString())
            when (recordResult) {
                is RecordResult.Error -> {
                    _statusFlow.update { RecognizeStatus.Error.RecordError(recordResult) }
                }
                is RecordResult.Success -> {
                    _statusFlow.update { RecognizeStatus.Recognizing }
                    val newStatus = when (val result = recognizeService.recognize(
                        token = token,
                        requiredServices = userPreferences.requiredServices,
                        file = recordFile
                    )) {
                        is RemoteRecognizeResult.Success -> {
                            val newTrack = saveTrackToStorage(result.data)
                            RecognizeStatus.Success(newTrack)
                        }
                        is RemoteRecognizeResult.NoMatches -> {
                            RecognizeStatus.NoMatches
                        }
                        is RemoteRecognizeResult.Error -> {
                            RecognizeStatus.Error.RemoteError(result)
                        }
                    }
                    _statusFlow.update { newStatus }
                }
            }
        }
    }

    private suspend fun saveTrackToStorage(fetchedTrack: Track): Track {
        coroutineScope {

        }
        val storedTrack = trackRepository.getByMbId(fetchedTrack.mbId)
        val newTrack = fetchedTrack.run {
            if (storedTrack != null) {
                copy(
                    metadata = this.metadata.copy(
                        isFavorite = storedTrack.metadata.isFavorite
                    )
                )
            } else {
                this
            }
        }
        trackRepository.insertOrReplace(newTrack)
        return newTrack
    }

    // FOR DEV PURPOSE
    fun recognizeRecordedFile(scope: CoroutineScope) {
        scope.launch {
            recognizeJob?.cancelAndJoin()
            recognizeJob = scope.launch {
                withContext(ioDispatcher) {
                    val userPreferences = preferencesRepository.userPreferencesFlow.first()
                    _statusFlow.update { RecognizeStatus.Recognizing }
                    val newStatus = when (val result = recognizeService.recognize(
                        token = token,
                        requiredServices = userPreferences.requiredServices,
                        file = recordFile
                    )) {
                        is RemoteRecognizeResult.Success -> {
                            val newTrack = saveTrackToStorage(result.data)
                            RecognizeStatus.Success(newTrack)
                        }
                        is RemoteRecognizeResult.NoMatches -> {
                            RecognizeStatus.NoMatches
                        }
                        is RemoteRecognizeResult.Error -> {
                            RecognizeStatus.Error.RemoteError(result)
                        }
                    }
                    _statusFlow.update { newStatus }
                }
            }.apply {
                invokeOnCompletion(::jobCancellationHandler)
            }
        }
    }

    // FOR DEV PURPOSE
    fun fakeRecognize(scope: CoroutineScope) {
        scope.launch {
            recognizeJob?.cancelAndJoin()
            recognizeJob = scope.launch {
                withContext(ioDispatcher) {
                    _statusFlow.update { RecognizeStatus.Listening }
                    delay(1000)
                    _statusFlow.update { RecognizeStatus.Recognizing }
                    delay(1000)
                    val newStatus = when (val result = recognizeService.fakeRecognize()) {
                        is RemoteRecognizeResult.Success -> {
                            val newTrack = saveTrackToStorage(result.data)
                            RecognizeStatus.Success(newTrack)
                        }
                        is RemoteRecognizeResult.NoMatches -> {
                            RecognizeStatus.NoMatches
                        }
                        is RemoteRecognizeResult.Error -> {
                            RecognizeStatus.Error.RemoteError(result)
                        }
                    }
                    _statusFlow.update { newStatus }
                }
            }.apply {
                invokeOnCompletion(::jobCancellationHandler)
            }
        }
    }

    //dev purpose
    fun setStatus(newStatus: RecognizeStatus) {
        _statusFlow.update { newStatus }
    }

    companion object {
        private const val RECORD_FILE_NAME = "mr_record.m4a"
        private const val RECORD_DURATION_IN_MILLIS = 6_000L
    }

}











