package com.mrsep.musicrecognizer.util

import com.mrsep.musicrecognizer.domain.model.Track
import java.time.Instant
import java.time.LocalDate

fun getFakeTrackList(startIndex: Int, endIndex: Int, inclusive: Boolean, favorites: Boolean): List<Track> {
    return List(if (inclusive) endIndex - startIndex + 1 else endIndex - startIndex) { index ->
        Track(
            mbId = "${index + startIndex}",
            title = "Track #${index + startIndex}",
            artist = "Artist #${index + startIndex}",
            releaseDate = LocalDate.now(),
            album = "Album #${index + startIndex}",
            lyrics = "lyrics #${index + startIndex}",
            links = Track.Links(
                artwork = "",
                spotify = "",
                youtube = "",
                soundCloud = "",
                appleMusic = "",
                musicBrainz = "",
                deezer = "",
                napster = "",
            ),
            metadata = Track.Metadata(
                lastRecognitionDate = Instant.ofEpochSecond(index.toLong()),
                isFavorite = favorites
            )
        )
    }
}