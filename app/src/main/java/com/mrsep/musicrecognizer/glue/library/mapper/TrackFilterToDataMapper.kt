package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.*
import com.mrsep.musicrecognizer.feature.library.domain.model.*
import javax.inject.Inject

class TrackFilterToDataMapper @Inject constructor() : Mapper<TrackFilter, TrackDataFilter> {

    override fun map(input: TrackFilter): TrackDataFilter {
        return TrackDataFilter(
            favoritesMode = when (input.favoritesMode) {
                FavoritesMode.All -> DataFavoritesMode.All
                FavoritesMode.OnlyFavorites -> DataFavoritesMode.OnlyFavorites
                FavoritesMode.ExcludeFavorites -> DataFavoritesMode.ExcludeFavorites
            },
            dateRange = when (val range = input.dateRange) {
                RecognitionDateRange.Empty -> DataRecognitionDateRange.Empty
                is RecognitionDateRange.Selected -> DataRecognitionDateRange.Selected(
                    startDate = range.startDate,
                    endDate = range.endDate
                )
            },
            sortBy = when (input.sortBy) {
                SortBy.RecognitionDate -> DataSortBy.RecognitionDate
                SortBy.Title -> DataSortBy.Title
                SortBy.Artist -> DataSortBy.Artist
                SortBy.ReleaseDate -> DataSortBy.ReleaseDate
            },
            orderBy = when (input.orderBy) {
                OrderBy.Asc -> DataOrderBy.Asc
                OrderBy.Desc -> DataOrderBy.Desc
            }
        )
    }

}