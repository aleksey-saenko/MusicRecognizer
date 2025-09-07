package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.preferences.RecognitionServiceConfig

interface ConfigValidator {

    suspend fun validate(config: RecognitionServiceConfig): ConfigValidationResult
}

sealed class ConfigValidationResult {

    data object Success : ConfigValidationResult()

    sealed class Error : ConfigValidationResult() {
        data object Empty : Error()
        data object AuthError : Error()
        data object ApiUsageLimited : Error()
        data object BadConnection : Error()
        data object UnknownError : Error()
    }
}
