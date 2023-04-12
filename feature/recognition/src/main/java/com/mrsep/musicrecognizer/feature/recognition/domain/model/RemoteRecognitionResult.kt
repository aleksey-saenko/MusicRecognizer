package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RemoteRecognitionResult<out T> {

    data class Success<out T>(val data: T) : RemoteRecognitionResult<T>()

    object NoMatches : RemoteRecognitionResult<Nothing>()

    sealed class Error : RemoteRecognitionResult<Nothing>() {

        object BadConnection : Error()
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

    inline fun <R> map(transform: (T) -> R): RemoteRecognitionResult<R> {
        return when (this) {
            is Success -> Success(transform(this.data))
            is NoMatches -> this
            is Error -> this
        }
    }

}