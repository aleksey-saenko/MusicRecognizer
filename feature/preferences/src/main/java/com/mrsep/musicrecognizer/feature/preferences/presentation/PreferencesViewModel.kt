package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.domain.preferences.AcrCloudConfig
import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.domain.preferences.FallbackPolicy
import com.mrsep.musicrecognizer.core.domain.preferences.HapticFeedback
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.preferences.UserPreferences
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.feature.preferences.RecognitionServiceStarter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PreferencesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val recognitionServiceStarter: RecognitionServiceStarter,
) : ViewModel() {

    val uiFlow = preferencesRepository.userPreferencesFlow
        .map { preferences -> PreferencesUiState.Success(preferences) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PreferencesUiState.Loading
        )

    fun setRecognitionProvider(value: RecognitionProvider) {
        viewModelScope.launch {
            preferencesRepository.setCurrentRecognitionProvider(value)
        }
    }

    fun setAuddConfig(value: AuddConfig) {
        viewModelScope.launch {
            preferencesRepository.setAuddConfig(value)
        }
    }

    fun setAcrCloudConfig(value: AcrCloudConfig) {
        viewModelScope.launch {
            preferencesRepository.setAcrCloudConfig(value)
        }
    }

    fun setDefaultAudioCaptureMode(value: AudioCaptureMode) {
        viewModelScope.launch {
            preferencesRepository.setDefaultAudioCaptureMode(value)
        }
    }

    fun setMainButtonLongPressAudioCaptureMode(value: AudioCaptureMode) {
        viewModelScope.launch {
            preferencesRepository.setMainButtonLongPressAudioCaptureMode(value)
        }
    }

    fun setNotificationServiceEnabled(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationServiceEnabled(value)
            if (value) {
                recognitionServiceStarter.startServiceHoldMode()
            } else {
                recognitionServiceStarter.stopServiceHoldMode()
            }
        }
    }

    fun setDynamicColorsEnabled(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColorsEnabled(value)
        }
    }

    fun setArtworkBasedThemeEnabled(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setArtworkBasedThemeEnabled(value)
        }
    }

    fun setRequiredMusicServices(services: List<MusicService>) {
        viewModelScope.launch {
            preferencesRepository.setRequiredMusicServices(services)
        }
    }

    fun setFallbackPolicy(fallbackPolicy: FallbackPolicy) {
        viewModelScope.launch {
            preferencesRepository.setFallbackPolicy(fallbackPolicy)
        }
    }

    fun setHapticFeedback(hapticFeedback: HapticFeedback) {
        viewModelScope.launch {
            preferencesRepository.setHapticFeedback(hapticFeedback)
        }
    }

    fun setThemeMode(value: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(value)
        }
    }

    fun setUsePureBlackForDarkTheme(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUsePureBlackForDarkTheme(value)
        }
    }

    fun setRecognizeOnStartup(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setRecognizeOnStartup(value)
        }
    }
}

@Immutable
internal sealed class PreferencesUiState {

    data object Loading : PreferencesUiState()

    data class Success(val preferences: UserPreferences) : PreferencesUiState()
}
