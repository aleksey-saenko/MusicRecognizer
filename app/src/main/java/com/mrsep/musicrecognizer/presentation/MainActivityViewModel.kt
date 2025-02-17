package com.mrsep.musicrecognizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    trackRepository: TrackRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _recognitionRequested = MutableStateFlow(false)
    val recognitionRequested = _recognitionRequested.asStateFlow()

    val unviewedTracksCount = trackRepository.getUnviewedCountFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val uiState = preferencesRepository.userPreferencesFlow
        .map { preferences -> MainActivityUiState.Success(
            onboardingCompleted = preferences.onboardingCompleted,
            recognizeOnStartup = preferences.recognizeOnStartup,
            notificationServiceEnabled = preferences.notificationServiceEnabled,
            dynamicColorsEnabled = preferences.dynamicColorsEnabled,
            themeMode = preferences.themeMode,
            usePureBlackForDarkTheme = preferences.usePureBlackForDarkTheme
        ) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )

    fun setRecognitionRequested(requested: Boolean) {
        if (requested) {
            requestRecognition(ignoreStartupPreference = true)
        } else {
            _recognitionRequested.update { false }
        }
    }

    fun requestRecognitionOnStartupIfPreferred() {
        requestRecognition(ignoreStartupPreference = false)
    }

    private fun requestRecognition(ignoreStartupPreference: Boolean) {
        viewModelScope.launch {
            val state = uiState
                .filterIsInstance<MainActivityUiState.Success>()
                .first()
            val shouldRequest = state.onboardingCompleted &&
                    (ignoreStartupPreference || state.recognizeOnStartup)
            if (shouldRequest) {
                _recognitionRequested.update { true }
            }
        }
    }
}

@Immutable
sealed class MainActivityUiState {

    data object Loading : MainActivityUiState()

    data class Success(
        val onboardingCompleted: Boolean,
        val recognizeOnStartup: Boolean,
        val notificationServiceEnabled: Boolean,
        val dynamicColorsEnabled: Boolean,
        val themeMode: ThemeMode,
        val usePureBlackForDarkTheme: Boolean,
    ) : MainActivityUiState()
}
