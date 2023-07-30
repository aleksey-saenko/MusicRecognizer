package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PreferencesViewModel @Inject constructor(
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

    fun setDeveloperModeEnabled(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDeveloperModeEnabled(value)
        }
    }

    fun setRequiredServices(requiredServices: UserPreferences.RequiredServices) {
        viewModelScope.launch {
            preferencesRepository.setRequiredServices(requiredServices)
        }
    }

    fun setSchedulePolicy(schedulePolicy: UserPreferences.SchedulePolicy) {
        viewModelScope.launch {
            preferencesRepository.setSchedulePolicy(schedulePolicy)
        }
    }

}

internal sealed interface PreferencesUiState {
    data object Loading : PreferencesUiState
    data class Success(val preferences: UserPreferences) : PreferencesUiState
}