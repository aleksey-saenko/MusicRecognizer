package com.mrsep.musicrecognizer.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

/*
 * MusicBrainz Recording Identifier uses as id (mbId)
 * https://musicbrainz.org/doc/MusicBrainz_Identifier
 */
@Immutable
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
        val appleMusic: String?,
        val musicBrainz: String?,
        val deezer: String?,
        val napster: String?
    )

    data class Metadata(
        val lastRecognitionDate: Long,
        val isFavorite: Boolean
    )

}