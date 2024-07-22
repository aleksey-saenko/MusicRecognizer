package com.mrsep.musicrecognizer.feature.library.domain.model

import java.time.Instant
import java.time.LocalDate

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val releaseDate: LocalDate?,
    val artworkThumbUrl: String?,
    val recognitionDate: Instant,
    val isViewed: Boolean,
    val isFavorite: Boolean,
)
