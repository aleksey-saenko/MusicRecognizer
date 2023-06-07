package com.mrsep.musicrecognizer.data.track

data class TrackFilterDo(
    val favoritesMode: DataFavoritesModeDo,
    val dateRange: DataRecognitionDateRangeDo,
    val sortBy: DataSortByDo,
    val orderBy: DataOrderByDo
) {

    companion object {
        val Empty = TrackFilterDo(
            favoritesMode = DataFavoritesModeDo.All,
            dateRange = DataRecognitionDateRangeDo.Empty,
            sortBy = DataSortByDo.RecognitionDate,
            orderBy = DataOrderByDo.Desc
        )
    }

}

enum class DataFavoritesModeDo { All, OnlyFavorites, ExcludeFavorites }
enum class DataSortByDo { RecognitionDate, Title, Artist, ReleaseDate }
enum class DataOrderByDo { Asc, Desc }

sealed class DataRecognitionDateRangeDo {

    data class Selected(
        val startDate: Long,
        val endDate: Long
    ) : DataRecognitionDateRangeDo()

    object Empty: DataRecognitionDateRangeDo()

}