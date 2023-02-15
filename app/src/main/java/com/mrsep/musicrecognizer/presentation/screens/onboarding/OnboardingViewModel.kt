package com.mrsep.musicrecognizer.presentation.screens.onboarding

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.BuildConfig
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var apiToken by mutableStateOf("")
        private set

    fun updateApiToken(input: String) {
        apiToken = input
    }

    init {
        viewModelScope.launch {
            apiToken = BuildConfig.AUDD_TOKEN.ifBlank {
                preferencesRepository.userPreferencesFlow.map { it.apiToken }.first()
            }
        }
    }

    fun applyToken() {
        viewModelScope.launch {
            preferencesRepository.saveApiToken(apiToken)
            preferencesRepository.setOnboardingCompleted(true)
        }
    }


}