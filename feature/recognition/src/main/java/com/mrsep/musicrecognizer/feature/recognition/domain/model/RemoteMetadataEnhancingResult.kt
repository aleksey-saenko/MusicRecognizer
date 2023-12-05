package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RemoteMetadataEnhancingResult {

    data class Success(val track: Track) : RemoteMetadataEnhancingResult()

    data object NoEnhancement : RemoteMetadataEnhancingResult()

    sealed class Error : RemoteMetadataEnhancingResult() {

        data object BadConnection : Error()

        data class HttpError(
            val code: Int,
            val message: String
        ) : Error()

        data class UnhandledError(
            val message: String = "",
            val cause: Throwable? = null
        ) : Error()

    }

}
