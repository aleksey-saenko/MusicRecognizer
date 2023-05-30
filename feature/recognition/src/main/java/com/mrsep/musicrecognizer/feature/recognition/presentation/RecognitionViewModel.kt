package com.mrsep.musicrecognizer.feature.recognition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.recognition.domain.*
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class SoundLevel {
    Silence, TooQuiet, Normal
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class RecognitionViewModel @Inject constructor(
    private val recognitionInteractor: ScreenRecognitionInteractor,
    private val recorderController: AudioRecorderController,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val recognitionState = recognitionInteractor.screenRecognitionStatus

    val isOffline = networkMonitor.isOffline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val maxAmplitudeFlow = recognitionState.flatMapLatest { state ->
        when (state) {
            is RecognitionStatus.Done,
            RecognitionStatus.Ready -> flowOf(0f)

            is RecognitionStatus.Recognizing -> recorderController.maxAmplitudeFlow
        }
    }

    fun recognizeTap() {
        if (recognitionState.value is RecognitionStatus.Recognizing) {
            recognitionInteractor.cancelAndResetStatus()
        } else {
            if (isOffline.value) {
                recognitionInteractor.launchOfflineRecognition(viewModelScope)
            } else {
                recognitionInteractor.launchRecognition(viewModelScope)
            }
        }
    }

    fun resetRecognitionResult() = recognitionInteractor.cancelAndResetStatus()

}
