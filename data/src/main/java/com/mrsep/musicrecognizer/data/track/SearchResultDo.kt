package com.mrsep.musicrecognizer.data.track

sealed class SearchResultDo {
    abstract val keyword: String

    data class Pending(
        override val keyword: String
    ) : SearchResultDo()

    data class Success(
        override val keyword: String,
        val data: List<TrackEntity>
    ) : SearchResultDo()

}