package com.mrsep.musicrecognizer.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.time.LocalDate

/*
 * MusicBrainz Recording Identifier (mbId) uses as id (primary) or random UUID (secondary)
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

    fun getSharedBody(): String {
        val albumAndYear = album?.let {
                alb -> releaseDate?.year?.let { year -> "$alb ($year)" } ?: album
        }
        return albumAndYear?.let { albAndYear ->
            "$title / $artist / $albAndYear"
        } ?: "$title / $artist"
    }

}