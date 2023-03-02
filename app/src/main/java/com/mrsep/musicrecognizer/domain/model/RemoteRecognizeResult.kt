package com.mrsep.musicrecognizer.domain.model

sealed class RemoteRecognizeResult<out T> {

    data class Success<out T>(val data: T) : RemoteRecognizeResult<T>()

    object NoMatches : RemoteRecognizeResult<Nothing>()

    sealed class Error : RemoteRecognizeResult<Nothing>() {

        object BadConnection : Error()
        object WrongToken : Error()
        object LimitReached : Error()

        data class HttpError(
            val code: Int,
            val message: String
        ): Error()

        data class UnhandledError(
            val message: String = "",
            val e: Throwable? = null
        ) : Error()

    }

    inline fun <R> map(transform: T.() -> R): RemoteRecognizeResult<R> {
        return when (this) {
            is Success -> Success(this.data.transform())
            is NoMatches -> this
            is Error -> this
        }
    }

}