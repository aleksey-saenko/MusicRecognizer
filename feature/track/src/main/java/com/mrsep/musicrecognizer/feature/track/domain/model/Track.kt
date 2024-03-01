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
    val trackLinks: List<TrackLink>,
    val properties: Properties
) {

    data class Properties(
        val lastRecognitionDate: Instant,
        val isFavorite: Boolean,
        val themeSeedColor: Int?
    )

}