package com.mrsep.musicrecognizer.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.BuildConfig
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.RecognizeService
import com.mrsep.musicrecognizer.domain.model.RemoteRecognizeResult
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

private const val AUDIO_SAMPLE_URL = "https://audd.tech/example.mp3"

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val recognizeService: RecognizeService
) : ViewModel() {

    private val _tokenState = MutableStateFlow<TokenState>(TokenState.Unchecked)
    val tokenState get() = _tokenState as StateFlow<TokenState>

    suspend fun getSavedToken(): String {
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            BuildConfig.AUDD_TOKEN.ifBlank {
                preferencesRepository.userPreferencesFlow.map { it.apiToken }.first()
            }
        }
    }

    fun testToken(testToken: String) {
//        if (testToken.isBlank()) {
//            _tokenState.update { MyTokenState.Wrong }
//            return
//        }
//        _tokenState.update { MyTokenState.Success }
//        return
        viewModelScope.launch {
            _tokenState.update { TokenState.Validating }

            val remoteResult = recognizeService.recognize(
                token = testToken,
                requiredServices = UserPreferences.RequiredServices(
                    spotify = true,
                    appleMusic = true,
                    deezer = true,
                    napster = true,
                    musicbrainz = true
                ),
                url = URL(AUDIO_SAMPLE_URL)
            )
//            Log.w("remoteResult", remoteResult.toString())
            val newState = when (remoteResult) {
                is RemoteRecognizeResult.Error.WrongToken -> TokenState.Wrong(
                    isLimitReached = remoteResult.isLimitReached
                )
                is RemoteRecognizeResult.Error -> TokenState.Error(remoteResult)
                else -> TokenState.Success
            }
            if (newState is TokenState.Success) {
                preferencesRepository.saveApiToken(testToken)
            }
            _tokenState.update { newState }
        }


    }

    fun resetTokenState() = _tokenState.update { TokenState.Unchecked }

    fun setOnboardingCompleted(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(value)
        }
    }

}

sealed class TokenState {
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

    data class Error(val rec: RemoteRecognizeResult.Error) : TokenState() {
        override val isValidating = false
        override val isSuccessToken = false
        override val isBadToken = false
        override val isValidationAllowed = true
    }

}