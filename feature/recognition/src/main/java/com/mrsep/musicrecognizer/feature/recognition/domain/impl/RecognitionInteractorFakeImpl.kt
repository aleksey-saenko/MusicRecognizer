package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

private val fakeTrack = Track(
    mbId = UUID.randomUUID().toString(),
    title = "Echoes",
    artist = "Pink Floyd",
    album = "Meddle",
    releaseDate = LocalDate.of(1971, 10, 30),
    lyrics = "lyrics stub",
    links = Track.Links(
        artwork = "https://upload.wikimedia.org/wikipedia/ru/1/1e/Meddle_album_cover.jpg",
        spotify = null,
        youtube = null,
        soundCloud = null,
        appleMusic = null,
        musicBrainz = null,
        deezer = null,
        napster = null
    ),
    metadata = Track.Metadata(
        lastRecognitionDate = Instant.now(),
        isFavorite = false
    )
)