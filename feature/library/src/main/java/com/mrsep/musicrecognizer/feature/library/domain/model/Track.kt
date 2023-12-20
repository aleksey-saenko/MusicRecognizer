package com.mrsep.musicrecognizer.feature.library.domain.model

import java.time.Instant
import java.time.LocalDate

/*
 * MusicBrainz Recording Identifier uses as id (primary) or random UUID (secondary)
 * https://musicbrainz.org/doc/MusicBrainz_Identifier
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val releaseDate: LocalDate?,
    val artworkUrl: String?,
    val properties: Properties
) {

    data class Properties(
        val lastRecognitionDate: Instant,
        val isFavorite: Boolean
    )

}