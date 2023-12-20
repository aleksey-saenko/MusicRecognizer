package com.mrsep.musicrecognizer.feature.library.presentation.model

import com.mrsep.musicrecognizer.core.common.util.AppDateTimeFormatter
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import java.time.ZoneId
import javax.annotation.concurrent.Immutable

@Immutable
internal data class TrackUi(
    val id: String,
    val title: String,
    val artist: String,
    val albumAndYear: String?,
    val artworkUrl: String?,
    val recognitionDate: String
)

internal fun Track.toUi(dateTimeFormatter: AppDateTimeFormatter) = TrackUi(
    id = this.id,
    title = this.title,
    artist = this.artist,
    albumAndYear = this.combineAlbumAndYear(),
    artworkUrl = this.artworkUrl,
    recognitionDate = dateTimeFormatter.formatRelativeToToday(
        this.properties.lastRecognitionDate.atZone(ZoneId.systemDefault())
    )
)

private fun Track.combineAlbumAndYear() = this.album?.let { alb ->
    releaseDate?.year?.let { year -> "$alb - $year" } ?: album
}