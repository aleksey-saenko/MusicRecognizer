package com.mrsep.musicrecognizer.feature.library.domain.model

sealed class SearchResult<T> {
    abstract val keyword: String

    data class Pending<T>(
        override val keyword: String
    ) : SearchResult<T>()

    data class Success<T>(
        override val keyword: String,
        val data: List<T>
    ) : SearchResult<T>() {
        val isEmpty get() = data.isEmpty()
    }

}