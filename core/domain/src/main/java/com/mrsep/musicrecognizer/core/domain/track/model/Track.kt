package com.mrsep.musicrecognizer.core.domain.track.model

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
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
    val recognizedBy: RecognitionProvider,
    val recognitionDate: Instant,
    val lyrics: String?,
    val artworkThumbUrl: String?,
    val artworkUrl: String?,
    val trackLinks: Map<MusicService, String>,
    val properties: Properties,
) {

    data class Properties(
        val isFavorite: Boolean,
        val isViewed: Boolean,
        val themeSeedColor: Int?,
    )
}
