package com.mrsep.musicrecognizer.feature.library.presentation.search

import androidx.compose.runtime.Immutable
import com.mrsep.musicrecognizer.core.common.util.AppDateTimeFormatter
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackDataField
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import com.mrsep.musicrecognizer.feature.library.presentation.model.toUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

@Immutable
internal sealed class SearchResultUi {
    abstract val query: String
    abstract val searchScope: ImmutableSet<TrackDataField>

    data class Pending(
        override val query: String,
        override val searchScope: ImmutableSet<TrackDataField>
    ) : SearchResultUi()

    data class Success(
        override val query: String,
        override val searchScope: ImmutableSet<TrackDataField>,
        val data: ImmutableList<TrackUi>
    ) : SearchResultUi() {
        val isEmpty get() = data.isEmpty()
    }

}

internal fun SearchResult.toUi(
    dateTimeFormatter: AppDateTimeFormatter
): SearchResultUi = when (this) {
    is SearchResult.Pending -> SearchResultUi.Pending(
        query = this.query,
        searchScope = this.searchScope.toImmutableSet(),
    )
    is SearchResult.Success -> SearchResultUi.Success(
        query = this.query,
        searchScope = this.searchScope.toImmutableSet(),
        data = this.data
            .map { track -> track.toUi(dateTimeFormatter) }
            .toImmutableList()
    )
}