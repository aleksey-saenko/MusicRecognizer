package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.recognition.di.MainScreenStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.*
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class RecognitionViewModel @Inject constructor(
    @MainScreenStatusHolder private val statusHolder: RecognitionStatusHolder,
    private val recognitionController: ScreenRecognitionController,
    private val recorderController: AudioRecorderController,
    private val vibrationManager: VibrationManager,
    preferencesRepository: PreferencesRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val recognitionState by statusHolder::status

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
        recognitionController.launchRecognition()
    }

    fun cancelRecognition() {
        recognitionController.cancelRecognition()
    }

    fun resetRecognitionResult() {
        statusHolder.resetFinalStatus()
    }

    fun vibrateOnTap() {
        vibrationManager.vibrateOnTap()
    }

    fun vibrateResult(isSuccess: Boolean) {
        vibrationManager.vibrateResult(isSuccess)
    }
}

internal interface ScreenRecognitionController {
    fun launchRecognition()
    fun cancelRecognition()
}

internal class ScreenRecognitionControllerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : ScreenRecognitionController {

    override fun launchRecognition() {
        appContext.startService(
            Intent(appContext, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_LAUNCH_RECOGNITION)
                .putExtra(RecognitionControlService.KEY_FOREGROUND_REQUESTED, false)
        )
    }

    override fun cancelRecognition() {
        appContext.startService(
            Intent(appContext, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_CANCEL_RECOGNITION)
        )
    }
}
