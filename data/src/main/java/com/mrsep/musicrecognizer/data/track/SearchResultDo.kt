package com.mrsep.musicrecognizer.data.track

sealed class SearchResultDo<out T> {
    abstract val keyword: String

    data class Pending(
        override val keyword: String
    ) : SearchResultDo<Nothing>()

    data class Success<T>(
        override val keyword: String,
        val data: List<T>
    ) : SearchResultDo<T>()

    inline fun <R> map(transform: (T) -> R): SearchResultDo<R> {
        return when (this) {
            is Pending -> this
            is Success -> Success(keyword = this.keyword, data = this.data.map(transform))
        }
    }

}