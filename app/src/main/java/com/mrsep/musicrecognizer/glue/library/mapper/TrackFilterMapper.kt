package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FavoritesModeDo
import com.mrsep.musicrecognizer.data.preferences.OrderByDo
import com.mrsep.musicrecognizer.data.preferences.SortByDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.library.domain.model.*
import javax.inject.Inject

class TrackFilterMapper @Inject constructor() :
    BidirectionalMapper<UserPreferencesDo.TrackFilterDo, TrackFilter> {

    override fun map(input: UserPreferencesDo.TrackFilterDo): TrackFilter {
        return TrackFilter(
            favoritesMode = when (input.favoritesMode) {
                FavoritesModeDo.All -> FavoritesMode.All
                FavoritesModeDo.OnlyFavorites -> FavoritesMode.OnlyFavorites
                FavoritesModeDo.ExcludeFavorites -> FavoritesMode.ExcludeFavorites
            },
            dateRange = input.dateRange,
            sortBy = when (input.sortBy) {
                SortByDo.RecognitionDate -> SortBy.RecognitionDate
                SortByDo.Title -> SortBy.Title
                SortByDo.Artist -> SortBy.Artist
                SortByDo.ReleaseDate -> SortBy.ReleaseDate
            },
            orderBy = when (input.orderBy) {
                OrderByDo.Asc -> OrderBy.Asc
                OrderByDo.Desc -> OrderBy.Desc
            }
        )
    }

    override fun reverseMap(input: TrackFilter): UserPreferencesDo.TrackFilterDo {
        return UserPreferencesDo.TrackFilterDo(
            favoritesMode = when (input.favoritesMode) {
                FavoritesMode.All -> FavoritesModeDo.All
                FavoritesMode.OnlyFavorites -> FavoritesModeDo.OnlyFavorites
                FavoritesMode.ExcludeFavorites -> FavoritesModeDo.ExcludeFavorites
            },
            dateRange = input.dateRange,
            sortBy = when (input.sortBy) {
                SortBy.RecognitionDate -> SortByDo.RecognitionDate
                SortBy.Title -> SortByDo.Title
                SortBy.Artist -> SortByDo.Artist
                SortBy.ReleaseDate -> SortByDo.ReleaseDate
            },
            orderBy = when (input.orderBy) {
                OrderBy.Asc -> OrderByDo.Asc
                OrderBy.Desc -> OrderByDo.Desc
            }
        )
    }
}
