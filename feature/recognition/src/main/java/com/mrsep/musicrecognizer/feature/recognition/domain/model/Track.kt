package com.mrsep.musicrecognizer.feature.recognition.domain.model

import java.time.Instant
import java.time.LocalDate

/*
 * MusicBrainz Recording Identifier (mbId) uses as id (primary) or random UUID (secondary)
 * https://musicbrainz.org/doc/MusicBrainz_Identifier
 */

data class Track(
    val mbId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val releaseDate: LocalDate?,
    val lyrics: String?,
    val artworkUrl: String?,
    val trackLinks: List<TrackLink>,
    val metadata: Metadata
) {

    data class Metadata(
        val lastRecognitionDate: Instant,
        val isFavorite: Boolean,
        val themeSeedColor: Int?
    )

}