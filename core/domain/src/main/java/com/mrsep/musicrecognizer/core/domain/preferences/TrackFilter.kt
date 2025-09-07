package com.mrsep.musicrecognizer.core.domain.preferences

data class TrackFilter(
    val favoritesMode: FavoritesMode,
    val sortBy: SortBy,
    val orderBy: OrderBy,
    val dateRange: LongRange,
) {
    val isDefault by lazy { this == getDefault() }

    companion object {
        fun getDefault() = TrackFilter(
            favoritesMode = FavoritesMode.All,
            sortBy = SortBy.RecognitionDate,
            orderBy = OrderBy.Desc,
            dateRange = Long.MIN_VALUE..Long.MAX_VALUE,
        )
    }
}

enum class FavoritesMode { All, OnlyFavorites, ExcludeFavorites }
enum class SortBy { RecognitionDate, Title, Artist, ReleaseDate }
enum class OrderBy { Asc, Desc }
