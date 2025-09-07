package com.mrsep.musicrecognizer.core.domain.track.model

sealed class SearchResult {
    abstract val query: String
    abstract val searchScope: Set<TrackDataField>

    data class Pending(
        override val query: String,
        override val searchScope: Set<TrackDataField>,
    ) : SearchResult()

    data class Success(
        override val query: String,
        override val searchScope: Set<TrackDataField>,
        val data: List<TrackPreview>
    ) : SearchResult()
}
