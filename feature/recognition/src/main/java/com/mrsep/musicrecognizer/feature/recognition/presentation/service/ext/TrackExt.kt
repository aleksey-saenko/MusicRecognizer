package com.mrsep.musicrecognizer.feature.recognition.presentation.service.ext

import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track

internal fun Track.artistWithAlbumFormatted(): String {
    val albumAndYear = album?.let { alb ->
        releaseDate?.year?.let { year -> "$alb ($year)" } ?: album
    }
    return albumAndYear?.let { albAndYear ->
        "$artist\n$albAndYear"
    } ?: artist
}

internal fun Track.getSharedBody() = "$title - ${this.artistWithAlbumFormatted()}"
