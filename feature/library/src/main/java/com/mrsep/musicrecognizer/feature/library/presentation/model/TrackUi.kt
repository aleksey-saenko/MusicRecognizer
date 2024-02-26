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
    val album: String?,
    val artworkUrl: String?,
    val recognitionDate: String
)

internal fun Track.toUi(dateTimeFormatter: AppDateTimeFormatter) = TrackUi(
    id = this.id,
    title = this.title,
    artist = this.artist,
    album = this.album,
    artworkUrl = this.artworkUrl,
    recognitionDate = dateTimeFormatter.formatRelativeToToday(
        this.properties.lastRecognitionDate.atZone(ZoneId.systemDefault())
    )
)