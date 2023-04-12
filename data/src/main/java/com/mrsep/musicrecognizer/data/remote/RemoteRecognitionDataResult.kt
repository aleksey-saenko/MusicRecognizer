package com.mrsep.musicrecognizer.data.remote

sealed class RemoteRecognitionDataResult<out T> {

    data class Success<out T>(val data: T) : RemoteRecognitionDataResult<T>()

    object NoMatches : RemoteRecognitionDataResult<Nothing>()

    sealed class Error : RemoteRecognitionDataResult<Nothing>() {

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

    inline fun <R> map(transform: (T) -> R): RemoteRecognitionDataResult<R> {
        return when (this) {
            is Success -> Success(transform(this.data))
            is NoMatches -> this
            is Error -> this
        }
    }


}