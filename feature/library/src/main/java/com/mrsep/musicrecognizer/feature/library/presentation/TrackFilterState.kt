package com.mrsep.musicrecognizer.feature.library.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mrsep.musicrecognizer.feature.library.domain.model.*

internal class TrackFilterState(
    initialTrackFilter: TrackFilter
) {
    var favoritesMode by mutableStateOf(initialTrackFilter.favoritesMode)
    var sortBy by mutableStateOf(initialTrackFilter.sortBy)
    var orderBy by mutableStateOf(initialTrackFilter.orderBy)
    var startDate: Long? by mutableStateOf(
        (initialTrackFilter.dateRange as? RecognitionDateRange.Selected)?.startDate
    )
    var endDate: Long? by mutableStateOf(
        (initialTrackFilter.dateRange as? RecognitionDateRange.Selected)?.endDate
    )

    fun makeFilter() = TrackFilter(
        favoritesMode = favoritesMode,
        dateRange = RecognitionDateRange.of(
            startDate = startDate,
            endDate = endDate
        ),
        sortBy = sortBy,
        orderBy = orderBy
    )

    fun resetFilter() {
        val emptyFilter = TrackFilter.Empty
        favoritesMode = emptyFilter.favoritesMode
        sortBy = emptyFilter.sortBy
        orderBy = emptyFilter.orderBy
        startDate = null
        endDate = null
    }

    companion object {
        val Saver: Saver<TrackFilterState, *> = listSaver(
            save = {
                listOf(
                    it.favoritesMode,
                    it.sortBy,
                    it.orderBy,
                    it.startDate,
                    it.endDate,
                )
            },
            restore = {
                TrackFilterState(
                    initialTrackFilter = TrackFilter(
                        favoritesMode = it[0] as FavoritesMode,
                        sortBy = it[1] as SortBy,
                        orderBy = it[2] as OrderBy,
                        dateRange = RecognitionDateRange.of(
                            startDate = it[3] as Long?,
                            endDate = it[4] as Long?
                        )
                    )
                )
            }
        )
    }
}

@Composable
internal fun rememberTrackFilterState(
    initialTrackFilter: TrackFilter
): TrackFilterState {
    return rememberSaveable(
        inputs = arrayOf(initialTrackFilter),
        saver = TrackFilterState.Saver
    ) {
        TrackFilterState(
            initialTrackFilter = initialTrackFilter
        )
    }
}