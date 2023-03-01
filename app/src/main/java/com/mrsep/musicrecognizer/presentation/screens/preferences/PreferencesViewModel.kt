package com.mrsep.musicrecognizer.presentation.screens.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiFlow = preferencesRepository.userPreferencesFlow
        .map { preferences -> PreferencesUiState.Success(preferences) }

    fun setOnboardingCompleted(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(value)
        }
    }

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

    fun setVisibleLinks(visibleLinks: UserPreferences.VisibleLinks) {
        viewModelScope.launch {
            preferencesRepository.setVisibleLinks(visibleLinks)
        }
    }

}

sealed interface PreferencesUiState {
    object Loading : PreferencesUiState
    data class Success(val preferences: UserPreferences) : PreferencesUiState
}