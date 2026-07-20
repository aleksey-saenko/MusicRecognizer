package com.mrsep.musicrecognizer.feature.recognition.service.floating

import androidx.compose.runtime.mutableStateOf
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.domain.preferences.HapticFeedback
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataFetchManager
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.feature.recognition.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.di.FloatingButtonStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.platform.VibrationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class FloatingWindowSharedModel(
    @FloatingButtonStatusHolder private val statusHolder: RecognitionStatusHolder,
    private val preferencesRepository: PreferencesRepository,
    private val trackRepository: TrackRepository,
    private val metadataFetchManager: TrackMetadataFetchManager,
    private val coroutineScope: CoroutineScope,
    private val vibrationManager: VibrationManager,
) {
    val isLeftAnchored = mutableStateOf(false)
    val isShowingLyrics = mutableStateOf(false)

    val preferences = preferencesRepository.userPreferencesFlow
        .map {
            FloatingWindowUserPreferences(
                defaultAudioCaptureMode = it.defaultAudioCaptureMode,
                mainButtonLongPressAudioCaptureMode = it.mainButtonLongPressAudioCaptureMode,
                useAltDeviceSoundSource = it.useAltDeviceSoundSource,
                hapticFeedback = it.hapticFeedback
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val uiState = statusHolder.status.flatMapLatest { status ->
        val track = ((status as? RecognitionStatus.Done)?.result as? RecognitionResult.Success)?.track
                ?: return@flatMapLatest flowOf(FloatingWindowUiState(status))
        combine(
            flow = trackRepository.getTrackFlow(track.id),
            flow2 = metadataFetchManager.isLyricsFetcherRunning(track.id)
        ) { track, isLyricsFetcherRunning ->
            if (track != null) {
                FloatingWindowUiState(status, track, isLyricsFetcherRunning)
            } else {
                FloatingWindowUiState(status)
            }
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FloatingWindowUiState(RecognitionStatus.Ready)
    )

    fun dismissRecognitionResult() {
        when (val status = statusHolder.status.value) {
            is RecognitionStatus.Done -> when (val result = status.result) {
                is RecognitionResult.Success -> coroutineScope.launch {
                    trackRepository.setViewed(result.track.id, true)
                }
                else -> Unit
            }
            else -> Unit
        }
        statusHolder.resetFinalStatus()
    }

    fun setFloatingButtonEnabled(value: Boolean) {
        coroutineScope.launch {
            preferencesRepository.setFloatingButtonEnabled(value)
        }
    }

    fun vibrateOnTap() {
        vibrationManager.vibrateOnTap()
    }

    fun vibrateSuccess() {
        vibrationManager.vibrateSuccess()
    }

    fun vibrateFailure() {
        vibrationManager.vibrateFailure()
    }
}

internal data class FloatingWindowUserPreferences(
    val defaultAudioCaptureMode: AudioCaptureMode,
    val mainButtonLongPressAudioCaptureMode: AudioCaptureMode,
    val useAltDeviceSoundSource: Boolean,
    val hapticFeedback: HapticFeedback,
)

internal data class FloatingWindowUiState(
    val recognitionStatus: RecognitionStatus,
    val currentTrack: Track? = null,
    val isLyricsFetcherRunning: Boolean = false,
)
