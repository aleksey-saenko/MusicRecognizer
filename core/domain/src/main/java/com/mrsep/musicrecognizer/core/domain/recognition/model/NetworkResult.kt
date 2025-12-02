package com.mrsep.musicrecognizer.core.domain.recognition.model

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
}

sealed class NetworkError : NetworkResult<Nothing>() {
    data class HttpError(val code: Int, val message: String? = null) : NetworkError() {
        val isClientError get() = code in 400..499
        val isServerError get() = code in 500..599
    }
    data class BadConnection(val message: String? = null) : NetworkError()
    data class UnhandledError(val message: String, val cause: Throwable? = null) : NetworkError()
}
