package com.mrsep.musicrecognizer.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.BuildConfig
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    suspend fun getSavedToken(): String {
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            BuildConfig.AUDD_TOKEN.ifBlank {
                preferencesRepository.userPreferencesFlow.map { it.apiToken }.first()
            }
        }
    }

    suspend fun validateAndSaveToken(token: String): Boolean {
        return if (token.isBlank()) {
            true
//            false
        } else {
            withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                delay(1_000)
                preferencesRepository.saveApiToken(token)
                true
            }
        }
    }

    fun setOnboardingCompleted(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(value)
        }
    }

}