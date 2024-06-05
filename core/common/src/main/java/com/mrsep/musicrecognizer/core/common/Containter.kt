package com.mrsep.musicrecognizer.core.common

sealed class Container<out T, out E>

data class Success<T>(val value: T) : Container<T, Nothing>()

data class Error<E>(val reason: E) : Container<Nothing, E>()

inline fun <T, E>Container<T, E>.onSuccess(block: T.() -> Unit): Container<T, E> {
    if (this is Success) {
        this.value.block()
    }
    return this
}

inline fun <T, E>Container<T, E>.onError(block: E.() -> Unit): Container<T, E> {
    if (this is Error) {
        this.reason.block()
    }
    return this
}

inline fun <T, E, R, F>Container<T, E>.map(successMapper: (T) -> R, errorMapper: (E) -> F): Container<R, F> {
    return when (this) {
        is Error -> Error(errorMapper(this.reason))
        is Success -> Success(successMapper(this.value))
    }
}
