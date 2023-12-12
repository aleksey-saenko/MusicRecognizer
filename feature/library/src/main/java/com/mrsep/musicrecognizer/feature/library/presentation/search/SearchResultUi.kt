package com.mrsep.musicrecognizer.feature.library.presentation.search

import androidx.compose.runtime.Immutable
import com.mrsep.musicrecognizer.core.common.util.AppDateTimeFormatter
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import com.mrsep.musicrecognizer.feature.library.presentation.model.toUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
internal sealed class SearchResultUi {
    abstract val keyword: String

    data class Pending(
        override val keyword: String
    ) : SearchResultUi()

    data class Success(
        override val keyword: String,
        val data: ImmutableList<TrackUi>
    ) : SearchResultUi() {
        val isEmpty get() = data.isEmpty()
    }

}

internal fun SearchResult.toUi(
    dateTimeFormatter: AppDateTimeFormatter
): SearchResultUi = when (this) {
    is SearchResult.Pending -> SearchResultUi.Pending(this.keyword)
    is SearchResult.Success -> SearchResultUi.Success(
        keyword = this.keyword,
        data = this.data
            .map { track -> track.toUi(dateTimeFormatter) }
            .toImmutableList()
    )
}