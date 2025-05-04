package com.mrsep.musicrecognizer.core.domain.track.model

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration

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
    val lyrics: Lyrics?,
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
