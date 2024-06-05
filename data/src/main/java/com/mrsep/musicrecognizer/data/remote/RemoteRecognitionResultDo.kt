package com.mrsep.musicrecognizer.data.remote

import com.mrsep.musicrecognizer.data.track.TrackEntity

sealed class RemoteRecognitionResultDo {

    data class Success(val data: TrackEntity) : RemoteRecognitionResultDo()

    data object NoMatches : RemoteRecognitionResultDo()

    sealed class Error : RemoteRecognitionResultDo() {

        data object BadConnection : Error()

        data class BadRecording(val message: String = "") : Error()

        data object AuthError : Error()

        data object ApiUsageLimited : Error()

        data class HttpError(
            val code: Int,
            val message: String
        ) : Error()

        data class UnhandledError(
            val message: String = "",
            val e: Throwable? = null
        ) : Error()
    }
}
