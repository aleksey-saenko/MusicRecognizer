package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.onboarding.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.onboarding.domain.RecognitionService
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.TokenValidationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val recognitionService: RecognitionService,
) : ViewModel() {

    // FIXME: need to save user input between process recreation
    // u can save token in rememberSaveable in Composable or in bundle savedInstantState
    private val _uiState = MutableStateFlow<TokenPageUiState>(TokenPageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val initToken = preferencesRepository.userPreferencesFlow.first().apiToken
            _uiState.update {
                TokenPageUiState.Success(
                    token = initToken,
                    tokenValidationStatus = TokenValidationStatus.Unchecked
                )
            }

        }
    }

    fun testToken() {
        val testToken = (_uiState.value as? TokenPageUiState.Success)?.token ?: return
        viewModelScope.launch {
            _uiState.update {
                TokenPageUiState.Success(
                    token = testToken,
                    tokenValidationStatus = TokenValidationStatus.Validating
                )
            }
            val remoteValidationStatus = recognitionService.validateToken(testToken)
            if (remoteValidationStatus is TokenValidationStatus.Success) {
                preferencesRepository.saveApiToken(testToken)
            }
            _uiState.update {
                TokenPageUiState.Success(
                    token = testToken,
                    tokenValidationStatus = remoteValidationStatus
                )
            }
        }

    }

    fun setTokenField(value: String) {
        _uiState.update {
            TokenPageUiState.Success(
                token = value,
                tokenValidationStatus = TokenValidationStatus.Unchecked
            )
        }
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