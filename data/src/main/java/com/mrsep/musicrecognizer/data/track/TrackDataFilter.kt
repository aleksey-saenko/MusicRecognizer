package com.mrsep.musicrecognizer.data.track

data class TrackDataFilter(
    val favoritesMode: DataFavoritesMode,
    val dateRange: DataRecognitionDateRange,
    val sortBy: DataSortBy,
    val orderBy: DataOrderBy
) {

    companion object {
        val Empty = TrackDataFilter(
            favoritesMode = DataFavoritesMode.All,
            dateRange = DataRecognitionDateRange.Empty,
            sortBy = DataSortBy.RecognitionDate,
            orderBy = DataOrderBy.Desc
        )
    }

}

enum class DataFavoritesMode { All, OnlyFavorites, ExcludeFavorites }
enum class DataSortBy { RecognitionDate, Title, Artist, ReleaseDate }
enum class DataOrderBy { Asc, Desc }

sealed class DataRecognitionDateRange {

    data class Selected(
        val startDate: Long,
        val endDate: Long
    ) : DataRecognitionDateRange()

    object Empty: DataRecognitionDateRange()

}