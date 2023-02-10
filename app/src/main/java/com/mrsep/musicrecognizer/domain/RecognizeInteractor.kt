package com.mrsep.musicrecognizer.domain

import android.content.Context
import com.mrsep.musicrecognizer.di.ApplicationScope
import com.mrsep.musicrecognizer.domain.model.RecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.time.Duration
import javax.inject.Inject

class RecognizeInteractor @Inject constructor(
    @ApplicationContext appContext: Context,
    @ApplicationScope private val appScope: CoroutineScope,
    private val recognizeService: RecognizeService,
    private val trackRepository: TrackRepository,
    private val myRecorder: RecorderController
) {

    private val _statusFlow: MutableStateFlow<RecognizeStatus> =
        MutableStateFlow(RecognizeStatus.Ready)
    val statusFlow: StateFlow<RecognizeStatus> get() = _statusFlow

    val recordFile =
        File("${appContext.filesDir.absolutePath}/${RECORD_FILE_NAME}")

    private var recognizeJob: Job? = null

    fun launchRecognizeOrCancel(scope: CoroutineScope) {
        if (recognizeJob?.isActive == true) {
            appScope.launch { cancelRecognize() }
        } else {
            recognizeJob = scope.launch { launchRecognize() }
        }
    }

    private suspend fun cancelRecognize() {
        withContext(Dispatchers.IO) {
            recognizeJob?.cancelAndJoin()
            recognizeJob = null
            _statusFlow.update { RecognizeStatus.Ready }
        }
    }

    private suspend fun launchRecognize() {
        withContext(Dispatchers.IO) {
            _statusFlow.update { RecognizeStatus.Listening }
            val recordResult = myRecorder.recordAudioToFile(
                recordFile,
                Duration.ofMillis(RECORD_DURATION_IN_MILLIS)
            )
            when (recordResult) {
                is RecordResult.Error -> {
                    _statusFlow.update { RecognizeStatus.Failure }
                }
                is RecordResult.Success -> {
                    _statusFlow.update { RecognizeStatus.Recognizing }

                    val savedTrack = when (val result = recognizeService.recognize(recordFile)) {
                        is RecognizeResult.Success -> {
                            val newTrack = saveTrackToStorage(result.data)
                            RecognizeResult.Success(newTrack)
                        }
                        else -> result
                    }

                    _statusFlow.update { RecognizeStatus.Success(savedTrack) }
                }
            }
        }
    }

    private suspend fun saveTrackToStorage(track: Track): Track {
        val storedTrack = trackRepository.getUnique(track.mbId)
        val newTrack = track.run {
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
            if (recognizeJob != null) {
                cancelRecognize()
            }
            recognizeJob = scope.launch {
                withContext(Dispatchers.IO) {
                    _statusFlow.update { RecognizeStatus.Recognizing }
                    val result = recognizeService.recognize(recordFile)
                    _statusFlow.update { RecognizeStatus.Success(result) }
                }
            }
        }
    }

    // FOR DEV PURPOSE
    fun fakeRecognize(scope: CoroutineScope) {
        scope.launch {
            if (recognizeJob != null) {
                cancelRecognize()
            }
            recognizeJob = scope.launch {
                withContext(Dispatchers.IO) {
                    _statusFlow.update { RecognizeStatus.Listening }
                    delay(500)
                    _statusFlow.update { RecognizeStatus.Recognizing }
                    delay(500)
                    val result = recognizeService.fakeRecognize()
                    _statusFlow.update { RecognizeStatus.Success(result) }
                }
            }
        }
    }

    companion object {
        private const val RECORD_FILE_NAME = "mr_record.m4a"
        private const val RECORD_DURATION_IN_MILLIS = 12_000L
    }

}





