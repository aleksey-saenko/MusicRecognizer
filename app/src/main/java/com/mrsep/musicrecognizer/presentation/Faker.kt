package com.mrsep.musicrecognizer.presentation

import com.mrsep.musicrecognizer.domain.model.Track
import java.time.LocalDate

val fakeTrackList = (1..10).map { index ->
    Track(
        mbId = "$index",
        title = "Wish you were here",
        artist = "Pink Floyd",
        releaseDate = LocalDate.now(),
        album = "Meddle",
        lyrics = "lyrics $index",
        links = Track.Links(
            artwork = "",
            spotify = "",
            youtube = "",
            appleMusic = "",
            musicBrainz = "",
            deezer = "",
            napster = "",
        ),
        metadata = Track.Metadata(
            lastRecognitionDate = 0L,
            isFavorite = true
        )
    )
}.toList()