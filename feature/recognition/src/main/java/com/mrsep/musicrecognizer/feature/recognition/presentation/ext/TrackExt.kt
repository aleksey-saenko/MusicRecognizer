package com.mrsep.musicrecognizer.feature.recognition.presentation.ext

import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track

fun Track.artistWithAlbumFormatted(): String {
    val albumAndYear = album?.let { alb ->
        releaseDate?.year?.let { year -> "$alb ($year)" } ?: album
    }
    return albumAndYear?.let { albAndYear ->
        "$artist - $albAndYear"
    } ?: artist
}

fun Track.getSharedBody() = "$title - ${this.artistWithAlbumFormatted()}"