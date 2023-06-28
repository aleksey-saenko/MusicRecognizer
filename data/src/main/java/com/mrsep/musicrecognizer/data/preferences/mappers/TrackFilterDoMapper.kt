package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.UserPreferencesProto.TrackFilterProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FavoritesModeDo
import com.mrsep.musicrecognizer.data.preferences.OrderByDo
import com.mrsep.musicrecognizer.data.preferences.SortByDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.TrackFilterDo
import javax.inject.Inject

class TrackFilterDoMapper @Inject constructor() :
    BidirectionalMapper<TrackFilterProto, TrackFilterDo> {

    override fun map(input: TrackFilterProto): TrackFilterDo {
        return TrackFilterDo(
            favoritesMode = when (input.favoritesMode) {
                FavoritesModeProto.ALL -> FavoritesModeDo.All
                FavoritesModeProto.ONLY_FAVORITES -> FavoritesModeDo.OnlyFavorites
                FavoritesModeProto.EXCLUDE_FAVORITES -> FavoritesModeDo.ExcludeFavorites
                FavoritesModeProto.UNRECOGNIZED, null -> FavoritesModeDo.All
            },
            sortBy = when (input.sortBy) {
                SortByProto.RECOGNITION_DATE -> SortByDo.RecognitionDate
                SortByProto.TITLE -> SortByDo.Title
                SortByProto.ARTIST -> SortByDo.Artist
                SortByProto.RELEASE_DATE -> SortByDo.ReleaseDate
                SortByProto.UNRECOGNIZED, null -> SortByDo.RecognitionDate
            },
            orderBy = when (input.orderBy) {
                OrderByProto.ASC -> OrderByDo.Asc
                OrderByProto.DESC -> OrderByDo.Desc
                OrderByProto.UNRECOGNIZED, null -> OrderByDo.Desc
            },
            dateRange = input.startDate..input.endDate
        )
    }

    override fun reverseMap(input: TrackFilterDo): TrackFilterProto {
        return TrackFilterProto.newBuilder()
            .setFavoritesMode(
                when (input.favoritesMode) {
                    FavoritesModeDo.All -> FavoritesModeProto.ALL
                    FavoritesModeDo.OnlyFavorites -> FavoritesModeProto.ONLY_FAVORITES
                    FavoritesModeDo.ExcludeFavorites -> FavoritesModeProto.EXCLUDE_FAVORITES
                }
            )
            .setSortBy(
                when (input.sortBy) {
                    SortByDo.RecognitionDate -> SortByProto.RECOGNITION_DATE
                    SortByDo.Title -> SortByProto.TITLE
                    SortByDo.Artist -> SortByProto.ARTIST
                    SortByDo.ReleaseDate -> SortByProto.RELEASE_DATE
                }
            )
            .setOrderBy(
                when (input.orderBy) {
                    OrderByDo.Asc -> OrderByProto.ASC
                    OrderByDo.Desc -> OrderByProto.DESC
                }
            )
            .setStartDate(input.dateRange.first)
            .setEndDate(input.dateRange.last)
            .build()
    }

}