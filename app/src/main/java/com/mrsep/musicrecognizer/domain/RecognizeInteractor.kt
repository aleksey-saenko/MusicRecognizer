package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.RecognizeResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class RecognizeInteractor @Inject constructor(
    private val recorderController: RecorderController,
    private val recognizeService: RecognizeService,
    private val trackRepository: TrackRepository
) {

    private val _statusFlow: MutableStateFlow<RecognizeStatus> =
        MutableStateFlow(RecognizeStatus.Ready)
    val statusFlow: StateFlow<RecognizeStatus> get() = _statusFlow

    private var recognizeJob: Job? = null

    fun recognize(scope: CoroutineScope) {
        if (recognizeJob != null) {
            recognizeJob?.cancel()
            recorderController.stopRecord()
            recognizeJob = null
            _statusFlow.update { RecognizeStatus.Ready }
        }
        recognizeJob = scope.launch {
            withContext(Dispatchers.IO) {
                _statusFlow.update { RecognizeStatus.Listening }
                recorderController.startRecord()
                delay(12_000)
                recorderController.stopRecord()
                _statusFlow.update { RecognizeStatus.Recognizing }

                val recognizeResult = recognizeService.recognize(recorderController.recordFile)

                val returnThis = when (recognizeResult) {
                    is RecognizeResult.Success -> {
                        val storedTrack = trackRepository.getUnique(recognizeResult.data.mbId)
                        val newTrack = recognizeResult.data.run {
                            if (storedTrack != null) {
                                copy(metadata = this.metadata.copy(isFavorite = storedTrack.metadata.isFavorite))
                            } else {
                                this
                            }
                        }
                        trackRepository.insertOrReplace(newTrack)
                        RecognizeResult.Success(newTrack)
                    }
                    else -> recognizeResult
                }

                _statusFlow.update { RecognizeStatus.Success(returnThis) }
            }
        }
    }

    fun cancelRecognize() = recognizeJob?.cancel()

    // FOR DEV PURPOSE
    fun recognizeRecordedFile(scope: CoroutineScope) {
        if (recognizeJob != null) {
            recognizeJob?.cancel()
            recorderController.stopRecord()
            recognizeJob = null
            _statusFlow.update { RecognizeStatus.Ready }
        }
        recognizeJob = scope.launch {
            withContext(Dispatchers.IO) {
                _statusFlow.update { RecognizeStatus.Recognizing }
                val result = recognizeService.recognize(recorderController.recordFile)
                _statusFlow.update { RecognizeStatus.Success(result) }
            }
        }
    }

    // FOR DEV PURPOSE
    fun fakeRecognize(scope: CoroutineScope) {
        if (recognizeJob != null) {
            recognizeJob?.cancel()
            recorderController.stopRecord()
            recognizeJob = null
            _statusFlow.update { RecognizeStatus.Ready }
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





