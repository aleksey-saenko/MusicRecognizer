package com.mrsep.musicrecognizer.feature.recognition.service.ext

import com.mrsep.musicrecognizer.core.domain.track.model.Track


internal fun Track.artistWithAlbumFormatted(): String {
    val albumAndYear = album?.let { alb ->
        releaseDate?.year?.let { year -> "$alb ($year)" } ?: album
    }
    return albumAndYear?.let { albAndYear ->
        "$artist\n$albAndYear"
    } ?: artist
}

internal fun Track.getSharedBody() = "$title - ${this.artistWithAlbumFormatted()}"
