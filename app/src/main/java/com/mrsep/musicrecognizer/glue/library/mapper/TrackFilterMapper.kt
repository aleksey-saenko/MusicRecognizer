package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.*
import com.mrsep.musicrecognizer.feature.library.domain.model.*
import javax.inject.Inject

class TrackFilterMapper @Inject constructor() : Mapper<TrackFilter, TrackFilterDo> {

    override fun map(input: TrackFilter): TrackFilterDo {
        return TrackFilterDo(
            favoritesMode = when (input.favoritesMode) {
                FavoritesMode.All -> DataFavoritesModeDo.All
                FavoritesMode.OnlyFavorites -> DataFavoritesModeDo.OnlyFavorites
                FavoritesMode.ExcludeFavorites -> DataFavoritesModeDo.ExcludeFavorites
            },
            dateRange = when (val range = input.dateRange) {
                RecognitionDateRange.Empty -> DataRecognitionDateRangeDo.Empty
                is RecognitionDateRange.Selected -> DataRecognitionDateRangeDo.Selected(
                    startDate = range.startDate,
                    endDate = range.endDate
                )
            },
            sortBy = when (input.sortBy) {
                SortBy.RecognitionDate -> DataSortByDo.RecognitionDate
                SortBy.Title -> DataSortByDo.Title
                SortBy.Artist -> DataSortByDo.Artist
                SortBy.ReleaseDate -> DataSortByDo.ReleaseDate
            },
            orderBy = when (input.orderBy) {
                OrderBy.Asc -> DataOrderByDo.Asc
                OrderBy.Desc -> DataOrderByDo.Desc
            }
        )
    }

}