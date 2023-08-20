package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.onboarding.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.onboarding.domain.RecognitionService
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.TokenValidationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_USER_TOKEN = "KEY_USER_TOKEN"

@HiltViewModel
internal class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferencesRepository: PreferencesRepository,
    private val recognitionService: RecognitionService,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TokenPageUiState>(TokenPageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val initToken = savedStateHandle[KEY_USER_TOKEN]
                ?: preferencesRepository.userPreferencesFlow.first().apiToken
            _uiState.update {
                TokenPageUiState.Success(
                    token = initToken,
                    tokenValidationStatus = TokenValidationStatus.Unchecked
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
                    tokenValidationStatus = TokenValidationStatus.Error.EmptyToken
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                TokenPageUiState.Success(
                    token = userToken,
                    tokenValidationStatus = TokenValidationStatus.Validating
                )
            }
            val remoteValidationStatus = recognitionService.validateToken(userToken)
            if (remoteValidationStatus is TokenValidationStatus.Success) {
                preferencesRepository.saveApiToken(userToken)
            }
            _uiState.update {
                TokenPageUiState.Success(
                    token = userToken,
                    tokenValidationStatus = remoteValidationStatus
                )
            }
        }

    }

    fun skipTokenApplying() {
        val userToken = (_uiState.value as? TokenPageUiState.Success)?.token ?: ""
        _uiState.update {
            TokenPageUiState.Success(
                token = userToken,
                tokenValidationStatus = TokenValidationStatus.Success
            )
        }
    }

    fun setTokenField(value: String) {
        _uiState.update {
            TokenPageUiState.Success(
                token = value,
                tokenValidationStatus = TokenValidationStatus.Unchecked
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
        val tokenValidationStatus: TokenValidationStatus
    ) : TokenPageUiState()

}