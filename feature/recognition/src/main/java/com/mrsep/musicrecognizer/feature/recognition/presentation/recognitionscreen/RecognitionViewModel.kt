package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.recognition.domain.*
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
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
    private val preferencesRepository: PreferencesRepository,
    private val vibrationManager: VibrationManager,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val recognitionState = recognitionInteractor.screenRecognitionStatus

    val preferences = preferencesRepository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        null
    )

    val isOffline = networkMonitor.isOffline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val maxAmplitudeFlow = recognitionState.mapLatest { state ->
        state is RecognitionStatus.Recognizing
    }.distinctUntilChanged().flatMapLatest { isRecording ->
        if (isRecording) recorderController.maxAmplitudeFlow else flowOf(0f)
    }

    fun launchRecognition() {
        if (isOffline.value) {
            recognitionInteractor.launchOfflineRecognition(viewModelScope)
        } else {
            recognitionInteractor.launchRecognition(viewModelScope)
        }
    }

    fun cancelRecognition() {
        recognitionInteractor.cancelAndResetStatus()
    }

    fun resetRecognitionResult() = recognitionInteractor.cancelAndResetStatus()

    fun vibrateOnTap() {
        vibrationManager.vibrateOnTap()
    }

    fun vibrateResult(isSuccess: Boolean) {
        vibrationManager.vibrateResult(isSuccess)
    }

}
