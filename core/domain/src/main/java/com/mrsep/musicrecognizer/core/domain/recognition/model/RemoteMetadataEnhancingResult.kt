package com.mrsep.musicrecognizer.core.domain.recognition.model

import com.mrsep.musicrecognizer.core.domain.track.model.Track

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
