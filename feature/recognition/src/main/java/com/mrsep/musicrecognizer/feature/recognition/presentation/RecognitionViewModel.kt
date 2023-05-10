package com.mrsep.musicrecognizer.feature.recognition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.recognition.domain.*
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class RecognitionViewModel @Inject constructor(
    private val recognitionInteractor: ScreenRecognitionInteractor,
    private val enqueueRecognitionUseCase: EnqueueRecognitionUseCase,
    private val recorderController: AudioRecorderController
) : ViewModel() {

    val recognitionState = recognitionInteractor.screenRecognitionStatus

    val maxAmplitudeFlow = recognitionState.flatMapLatest { state ->
        when (state) {
            is RecognitionStatus.Done,
            RecognitionStatus.Ready -> flow { emit(0f) }
            is RecognitionStatus.Recognizing -> recorderController.maxAmplitudeFlow
        }
    }

    fun recognizeTap() {
        if (recognitionState.value is RecognitionStatus.Recognizing) {
            recognitionInteractor.cancelAndResetStatus()
        } else {
            recognitionInteractor.launchRecognition(viewModelScope)
        }
    }

    fun enqueueRecognitionAndResetResult() {
        viewModelScope.launch {
            when (val state = recognitionState.value) {
                is RecognitionStatus.Done -> when (state.result) {
                    is RecognitionResult.Error -> {
                        enqueueRecognitionUseCase(
                            audioRecording = state.result.audioRecording,
                            launch = true
                        )
                    }
                    is RecognitionResult.NoMatches -> {
                        enqueueRecognitionUseCase(
                            audioRecording = state.result.audioRecording,
                            launch = false
                        )
                    }
                    else -> {}
                }
                else -> {}
            }
            resetRecognitionResult()
        }
    }

    fun resetRecognitionResult() = recognitionInteractor.cancelAndResetStatus()


}
