package com.mrsep.musicrecognizer.feature.onboarding.domain.model

sealed class TokenValidationStatus {

    abstract val isValidationAllowed: Boolean

    object Unchecked : TokenValidationStatus() {
        override val isValidationAllowed = true
    }

    object Validating : TokenValidationStatus() {
        override val isValidationAllowed = false
    }

    object Success : TokenValidationStatus() {
        override val isValidationAllowed = false
    }

    sealed class Error : TokenValidationStatus() {
        override val isValidationAllowed = true

        data class WrongToken(val isLimitReached: Boolean) : Error()
        object BadConnection : Error()
        object UnknownError : Error()

    }

}