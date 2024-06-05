package com.mrsep.musicrecognizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.TrackRepository
import com.mrsep.musicrecognizer.domain.UserPreferences
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
        .map { preferences -> MainActivityUiState.Success(userPreferences = preferences) }
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
            val userPreferences = uiState
                .filterIsInstance<MainActivityUiState.Success>()
                .first()
                .userPreferences
            val shouldRequest = userPreferences.onboardingCompleted &&
                    (ignoreStartupPreference || userPreferences.recognizeOnStartup)
            if (shouldRequest) {
                _recognitionRequested.update { true }
            }
        }
    }
}

@Immutable
sealed class MainActivityUiState {

    data object Loading : MainActivityUiState()

    data class Success(val userPreferences: UserPreferences) : MainActivityUiState()
}
