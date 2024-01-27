package com.mrsep.musicrecognizer.feature.onboarding.domain.model

sealed class ConfigValidationStatus {

    abstract val isValidationAllowed: Boolean

    data object Unchecked : ConfigValidationStatus() {
        override val isValidationAllowed = true
    }

    data object Validating : ConfigValidationStatus() {
        override val isValidationAllowed = false
    }

    data object Success : ConfigValidationStatus() {
        override val isValidationAllowed = false
    }

    sealed class Error : ConfigValidationStatus() {
        override val isValidationAllowed = true

        data class WrongToken(val isLimitReached: Boolean) : Error()
        data object EmptyToken : Error()
        data object BadConnection : Error()
        data object UnknownError : Error()

    }

}