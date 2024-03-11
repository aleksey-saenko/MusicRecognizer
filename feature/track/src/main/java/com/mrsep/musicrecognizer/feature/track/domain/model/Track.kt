package com.mrsep.musicrecognizer.feature.track.domain.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val releaseDate: LocalDate?,
    val duration: Duration?,
    val recognizedAt: Duration?,
    val lyrics: String?,
    val artworkUrl: String?,
    val themeSeedColor: Int?,
    val trackLinks: List<TrackLink>,
    val recognitionDate: Instant,
    val isViewed: Boolean,
    val isFavorite: Boolean,
)