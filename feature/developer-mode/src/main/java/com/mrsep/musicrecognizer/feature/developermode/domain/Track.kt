package com.mrsep.musicrecognizer.feature.developermode.domain

import java.time.Instant
import java.time.LocalDate

data class Track(
    val mbId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val releaseDate: LocalDate?,
    val lyrics: String?,
    val links: Links,
    val metadata: Metadata
) {

    data class Links(
        val artwork: String?,
        val spotify: String?,
        val youtube: String?,
        val soundCloud: String?,
        val appleMusic: String?,
        val musicBrainz: String?,
        val deezer: String?,
        val napster: String?
    )

    data class Metadata(
        val lastRecognitionDate: Instant,
        val isFavorite: Boolean
    )

}