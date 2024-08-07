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
    val artworkThumbUrl: String?,
    val recognitionDate: String,
    val isViewed: Boolean,
)

internal fun Track.toUi(dateTimeFormatter: AppDateTimeFormatter) = TrackUi(
    id = this.id,
    title = this.title,
    artist = this.artist,
    album = this.album,
    artworkThumbUrl = this.artworkThumbUrl,
    recognitionDate = dateTimeFormatter.formatRelativeToToday(
        this.recognitionDate.atZone(ZoneId.systemDefault())
    ),
    isViewed = this.isViewed,
)
