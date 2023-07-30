package com.mrsep.musicrecognizer.feature.onboarding.domain.model

sealed class TokenValidationStatus {

    abstract val isValidationAllowed: Boolean

    data object Unchecked : TokenValidationStatus() {
        override val isValidationAllowed = true
    }

    data object Validating : TokenValidationStatus() {
        override val isValidationAllowed = false
    }

    data object Success : TokenValidationStatus() {
        override val isValidationAllowed = false
    }

    sealed class Error : TokenValidationStatus() {
        override val isValidationAllowed = true

        data class WrongToken(val isLimitReached: Boolean) : Error()
        data object BadConnection : Error()
        data object UnknownError : Error()

    }

}