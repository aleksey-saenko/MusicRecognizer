package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.onboarding.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.RemoteRecognitionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
//    private val recognitionService: RecognitionService,
) : ViewModel() {

    private val _tokenState = MutableStateFlow<TokenState>(TokenState.Unchecked)
    val tokenState = _tokenState.asStateFlow()

    val preferences = preferencesRepository.userPreferencesFlow

    fun testToken(testToken: String) {
        _tokenState.update { TokenState.Success }
        return
//        if (testToken.isBlank()) {
//            _tokenState.update { MyTokenState.Wrong }
//            return
//        }
//        _tokenState.update { MyTokenState.Success }
//        return

//        viewModelScope.launch {
//            _tokenState.update { TokenState.Validating }
//
//            val newState = when (val remoteResult = recognitionService.validateToken(testToken)) {
//                is RemoteRecognitionResult.Error.WrongToken -> TokenState.Wrong(
//                    isLimitReached = remoteResult.isLimitReached
//                )
//                is RemoteRecognitionResult.Error -> TokenState.Error(remoteResult)
//                else -> TokenState.Success
//            }
//            if (newState is TokenState.Success) {
//                preferencesRepository.saveApiToken(testToken)
//            }
//            _tokenState.update { newState }
//        }

    }

    fun resetTokenState() = _tokenState.update { TokenState.Unchecked }

    fun setOnboardingCompleted(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(value)
        }
    }

}

internal sealed class TokenState {
    abstract val isValidating: Boolean
    abstract val isSuccessToken: Boolean
    abstract val isBadToken: Boolean
    abstract val isValidationAllowed: Boolean

    object Unchecked : TokenState() {
        override val isValidating = false
        override val isSuccessToken = false
        override val isBadToken = false
        override val isValidationAllowed = true
    }
    object Validating : TokenState() {
        override val isValidating = true
        override val isSuccessToken = false
        override val isBadToken = false
        override val isValidationAllowed = false
    }
    object Success : TokenState() {
        override val isValidating = false
        override val isSuccessToken = true
        override val isBadToken = false
        override val isValidationAllowed = false
    }
    data class Wrong(val isLimitReached: Boolean) : TokenState() {
        override val isValidating = false
        override val isSuccessToken = false
        override val isBadToken = true
        override val isValidationAllowed = true
    }

    data class Error(val rec: RemoteRecognitionResult.Error) : TokenState() {
        override val isValidating = false
        override val isSuccessToken = false
        override val isBadToken = false
        override val isValidationAllowed = true
    }

}