package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PreferencesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiFlow = preferencesRepository.userPreferencesFlow
        .map { preferences -> PreferencesUiState.Success(preferences) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PreferencesUiState.Loading
        )

    fun setNotificationServiceEnabled(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationServiceEnabled(value)
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

    fun setRequiredServices(requiredServices: UserPreferences.RequiredServices) {
        viewModelScope.launch {
            preferencesRepository.setRequiredServices(requiredServices)
        }
    }

    fun setFallbackPolicy(fallbackPolicy: UserPreferences.FallbackPolicy) {
        viewModelScope.launch {
            preferencesRepository.setFallbackPolicy(fallbackPolicy)
        }
    }

    fun setApiToken(token: String) {
        viewModelScope.launch {
            preferencesRepository.setApiToken(token)
        }
    }

    fun setHapticFeedback(hapticFeedback: UserPreferences.HapticFeedback) {
        viewModelScope.launch {
            preferencesRepository.setHapticFeedback(hapticFeedback)
        }
    }

    fun setUseGridForLibrary(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUseGridForLibrary(value)
        }
    }

}

internal sealed interface PreferencesUiState {
    data object Loading : PreferencesUiState
    data class Success(val preferences: UserPreferences) : PreferencesUiState
}