package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.onboarding.domain.ConfigValidator
import com.mrsep.musicrecognizer.feature.onboarding.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.AuddConfig
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.ConfigValidationStatus
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
                    configValidationStatus = ConfigValidationStatus.Unchecked
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
                    configValidationStatus = ConfigValidationStatus.Error.EmptyToken
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                TokenPageUiState.Success(
                    token = userToken,
                    configValidationStatus = ConfigValidationStatus.Validating
                )
            }
            val auddConfig = AuddConfig(userToken)
            val validationStatus = configValidator.validate(auddConfig)
            if (validationStatus is ConfigValidationStatus.Success) {
                preferencesRepository.setAuddConfig(auddConfig)
            }
            _uiState.update {
                TokenPageUiState.Success(
                    token = userToken,
                    configValidationStatus = validationStatus
                )
            }
        }

    }

    fun skipTokenApplying() {
        val userToken = (_uiState.value as? TokenPageUiState.Success)?.token ?: ""
        _uiState.update {
            TokenPageUiState.Success(
                token = userToken,
                configValidationStatus = ConfigValidationStatus.Success
            )
        }
    }

    fun setTokenField(value: String) {
        _uiState.update {
            TokenPageUiState.Success(
                token = value,
                configValidationStatus = ConfigValidationStatus.Unchecked
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
        val configValidationStatus: ConfigValidationStatus
    ) : TokenPageUiState()

}