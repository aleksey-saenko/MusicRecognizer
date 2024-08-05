package com.mrsep.musicrecognizer.feature.library.domain.model

import java.time.Instant

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val artworkThumbUrl: String?,
    val recognitionDate: Instant,
    val isViewed: Boolean,
)
