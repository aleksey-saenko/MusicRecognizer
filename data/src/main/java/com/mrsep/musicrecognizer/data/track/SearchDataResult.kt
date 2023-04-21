package com.mrsep.musicrecognizer.data.track

sealed class SearchDataResult<out T> {
    abstract val keyword: String

    data class Pending(
        override val keyword: String
    ) : SearchDataResult<Nothing>()

    data class Success<T>(
        override val keyword: String,
        val data: List<T>
    ) : SearchDataResult<T>()

    inline fun <R> map(transform: (T) -> R): SearchDataResult<R> {
        return when (this) {
            is Pending -> this
            is Success -> Success(keyword = this.keyword, data = this.data.map(transform))
        }
    }

}