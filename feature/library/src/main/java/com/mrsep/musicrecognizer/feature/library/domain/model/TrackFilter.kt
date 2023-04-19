package com.mrsep.musicrecognizer.feature.library.domain.model

data class TrackFilter(
    val favoritesMode: FavoritesMode,
    val dateRange: RecognitionDateRange,
    val sortBy: SortBy,
    val orderBy: OrderBy
) {

    companion object {
        val Empty = TrackFilter(
            favoritesMode = FavoritesMode.All,
            dateRange = RecognitionDateRange.Empty,
            sortBy = SortBy.RecognitionDate,
            orderBy = OrderBy.Desc
        )
    }

}

enum class FavoritesMode { All, OnlyFavorites, ExcludeFavorites }
enum class SortBy { RecognitionDate, Title, Artist, ReleaseDate }
enum class OrderBy { Asc, Desc }

sealed class RecognitionDateRange {

    data class Selected(
        val startDate: Long,
        val endDate: Long
    ) : RecognitionDateRange()

    object Empty: RecognitionDateRange()

    companion object {
        fun of(startDate: Long?, endDate: Long?): RecognitionDateRange {
            return if (startDate == null && endDate == null) {
                Empty
            } else {
                Selected(
                    startDate = startDate ?: Long.MIN_VALUE,
                    endDate = endDate ?: Long.MAX_VALUE
                )
            }
        }
    }

}