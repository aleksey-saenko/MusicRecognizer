package com.mrsep.musicrecognizer.feature.library.domain.model

data class TrackFilter(
    val favoritesMode: FavoritesMode,
    val dateRange: LongRange,
    val sortBy: SortBy,
    val orderBy: OrderBy
) {
    val isDefault = favoritesMode == FavoritesMode.All &&
            dateRange.first == Long.MIN_VALUE && dateRange.last == Long.MAX_VALUE &&
            sortBy == SortBy.RecognitionDate &&
            orderBy == OrderBy.Desc

    companion object {
        fun getDefault() = TrackFilter(
            favoritesMode = FavoritesMode.All,
            dateRange = Long.MIN_VALUE..Long.MAX_VALUE,
            sortBy = SortBy.RecognitionDate,
            orderBy = OrderBy.Desc
        )
    }

}

enum class FavoritesMode { All, OnlyFavorites, ExcludeFavorites }
enum class SortBy { RecognitionDate, Title, Artist, ReleaseDate }
enum class OrderBy { Asc, Desc }