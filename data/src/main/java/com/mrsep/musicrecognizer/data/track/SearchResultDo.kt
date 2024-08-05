package com.mrsep.musicrecognizer.data.track

sealed class SearchResultDo {
    abstract val query: String
    abstract val searchScope: Set<TrackDataFieldDo>

    data class Pending(
        override val query: String,
        override val searchScope: Set<TrackDataFieldDo>,
    ) : SearchResultDo()

    data class Success(
        override val query: String,
        override val searchScope: Set<TrackDataFieldDo>,
        val data: List<TrackPreview>
    ) : SearchResultDo()
}
