package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RemoteRecognitionResult {

    data class Success(val track: Track) : RemoteRecognitionResult()

    object NoMatches : RemoteRecognitionResult()

    sealed class Error : RemoteRecognitionResult() {

        object BadConnection : Error()
        data class BadRecording(val cause: Throwable) : Error()
        data class WrongToken(val isLimitReached: Boolean) : Error()

        data class HttpError(
            val code: Int,
            val message: String
        ): Error()

        data class UnhandledError(
            val message: String = "",
            val t: Throwable? = null
        ) : Error()

    }

}

