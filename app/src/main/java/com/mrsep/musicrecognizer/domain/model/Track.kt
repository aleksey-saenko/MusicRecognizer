package com.mrsep.musicrecognizer.domain.model

import java.time.LocalDate

class Track(
    val title: String,
    val artist: String,
    val album: String?,
    val releaseDate: LocalDate?,
    val lyrics: String?,
    val links: Links,
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

}