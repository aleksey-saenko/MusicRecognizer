package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidationResult
import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_USER_TOKEN = "KEY_USER_TOKEN"

@HiltViewModel
internal class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferencesRepository: PreferencesRepository,
    private val configValidator: ConfigValidator,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TokenPageUiState>(TokenPageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val initToken = savedStateHandle[KEY_USER_TOKEN]
                ?: preferencesRepository.userPreferencesFlow.first().auddConfig.apiToken
            _uiState.update {
                TokenPageUiState.Success(
                    token = initToken,
                    isValidating = false,
                    configValidationResult = null
                )
            }
        }
    }

    fun applyTokenIfValid() {
        val userToken = (_uiState.value as? TokenPageUiState.Success)?.token ?: return
        if (userToken.isBlank()) {
            _uiState.update {
                TokenPageUiState.Success(
                    token = userToken,
                    isValidating = false,
                    configValidationResult = ConfigValidationResult.Error.Empty
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                TokenPageUiState.Success(
                    token = userToken,
                    isValidating = true,
                    configValidationResult = null
                )
            }
            val auddConfig = AuddConfig(userToken)
            val validationResult = configValidator.validate(auddConfig)
            if (validationResult is ConfigValidationResult.Success) {
                preferencesRepository.setAuddConfig(auddConfig)
            }
            _uiState.update {
                TokenPageUiState.Success(
                    token = userToken,
                    isValidating = false,
                    configValidationResult = validationResult
                )
            }
        }
    }

    fun setTokenField(value: String) {
        _uiState.update {
            TokenPageUiState.Success(
                token = value,
                isValidating = false,
                configValidationResult = null
            )
        }
        savedStateHandle[KEY_USER_TOKEN] = value
    }

    fun setOnboardingCompleted(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(value)
        }
    }
}

@Immutable
internal sealed class TokenPageUiState {

    data object Loading : TokenPageUiState()

    data class Success(
        val token: String,
        val isValidating: Boolean,
        val configValidationResult: ConfigValidationResult?,
    ) : TokenPageUiState()
}
