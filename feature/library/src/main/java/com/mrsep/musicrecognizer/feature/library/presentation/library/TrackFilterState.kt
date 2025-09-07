package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mrsep.musicrecognizer.core.domain.preferences.FavoritesMode
import com.mrsep.musicrecognizer.core.domain.preferences.OrderBy
import com.mrsep.musicrecognizer.core.domain.preferences.SortBy
import com.mrsep.musicrecognizer.core.domain.preferences.TrackFilter

@Stable
internal class TrackFilterState(
    initialTrackFilter: TrackFilter
) {
    var favoritesMode by mutableStateOf(initialTrackFilter.favoritesMode)
    var sortBy by mutableStateOf(initialTrackFilter.sortBy)
    var orderBy by mutableStateOf(initialTrackFilter.orderBy)
    var startDate: Long by mutableLongStateOf(
        initialTrackFilter.dateRange.first
    )
    var endDate: Long by mutableLongStateOf(
        initialTrackFilter.dateRange.last
    )

    fun makeFilter() = TrackFilter(
        favoritesMode = favoritesMode,
        dateRange = startDate..endDate,
        sortBy = sortBy,
        orderBy = orderBy
    )

    fun resetFilter() {
        val emptyFilter = TrackFilter.getDefault()
        favoritesMode = emptyFilter.favoritesMode
        sortBy = emptyFilter.sortBy
        orderBy = emptyFilter.orderBy
        startDate = Long.MIN_VALUE
        endDate = Long.MAX_VALUE
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
                        dateRange = it[3] as Long..it[4] as Long
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
