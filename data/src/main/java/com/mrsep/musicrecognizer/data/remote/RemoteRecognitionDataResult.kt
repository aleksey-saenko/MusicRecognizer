package com.mrsep.musicrecognizer.data.remote

import com.mrsep.musicrecognizer.data.track.TrackEntity

sealed class RemoteRecognitionDataResult {

    data class Success(val data: TrackEntity) : RemoteRecognitionDataResult()

    object NoMatches : RemoteRecognitionDataResult()

    sealed class Error : RemoteRecognitionDataResult() {

        object BadConnection : Error()
        object BadRecording: Error()
        data class WrongToken(val isLimitReached: Boolean) : Error()

        data class HttpError(
            val code: Int,
            val message: String
        ): Error()

        data class UnhandledError(
            val message: String = "",
            val e: Throwable? = null
        ) : Error()

    }

}