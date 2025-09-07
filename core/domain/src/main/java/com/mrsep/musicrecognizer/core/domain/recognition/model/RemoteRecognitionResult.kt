package com.mrsep.musicrecognizer.core.domain.recognition.model

import com.mrsep.musicrecognizer.core.domain.track.model.Track

sealed class RemoteRecognitionResult {

    data class Success(val track: Track) : RemoteRecognitionResult()

    data object NoMatches : RemoteRecognitionResult()

    sealed class Error : RemoteRecognitionResult() {

        data object BadConnection : Error()

        data class BadRecording(
            val message: String = "",
            val cause: Throwable? = null
        ) : Error()

        data object AuthError : Error()

        data object ApiUsageLimited : Error()

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
