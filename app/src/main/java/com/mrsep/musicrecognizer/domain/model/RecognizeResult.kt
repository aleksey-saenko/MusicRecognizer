package com.mrsep.musicrecognizer.domain.model

sealed class RecognizeResult<out T> {

    object InProgress : RecognizeResult<Nothing>()

    data class Success<out T>(val data: T) : RecognizeResult<T>()

    object NoMatches : RecognizeResult<Nothing>()

    sealed class Error : RecognizeResult<Nothing>() {

        object NoConnection : Error()
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

    inline fun <R> map(transform: T.() -> R): RecognizeResult<R> {
        return when (this) {
            is Success -> Success(this.data.transform())
            is InProgress -> this
            is NoMatches -> this
            is Error -> this
        }
    }

}