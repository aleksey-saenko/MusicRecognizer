package com.mrsep.musicrecognizer.feature.library.domain.model

sealed class SearchResult {
    abstract val keyword: String

    data class Pending(
        override val keyword: String
    ) : SearchResult()

    data class Success(
        override val keyword: String,
        val data: List<Track>
    ) : SearchResult()

}