package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.preferences.domain.AcrCloudConfig
import com.mrsep.musicrecognizer.feature.preferences.domain.AuddConfig
import com.mrsep.musicrecognizer.feature.preferences.domain.MusicService
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRouter
import com.mrsep.musicrecognizer.feature.preferences.domain.RecognitionProvider
import com.mrsep.musicrecognizer.feature.preferences.domain.ThemeMode
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PreferencesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val preferencesRouter: PreferencesRouter,
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

    fun setNotificationServiceEnabled(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationServiceEnabled(value)
            if (value) {
                preferencesRouter.startServiceHoldMode()
            } else {
                preferencesRouter.stopServiceHoldMode()
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

    fun setFallbackPolicy(fallbackPolicy: UserPreferences.FallbackPolicy) {
        viewModelScope.launch {
            preferencesRepository.setFallbackPolicy(fallbackPolicy)
        }
    }

    fun setHapticFeedback(hapticFeedback: UserPreferences.HapticFeedback) {
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