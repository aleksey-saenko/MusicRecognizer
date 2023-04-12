package com.mrsep.musicrecognizer.data.track.util

import com.mrsep.musicrecognizer.data.track.TrackEntity
import java.time.Instant
import java.time.LocalDate

internal fun getFakeTrackList(startIndex: Int, endIndex: Int, inclusive: Boolean, favorites: Boolean): List<TrackEntity> {
    return List(if (inclusive) endIndex - startIndex + 1 else endIndex - startIndex) { index ->
        TrackEntity(
            mbId = "${index + startIndex}",
            title = "Track #${index + startIndex}",
            artist = "Artist #${index + startIndex}",
            releaseDate = LocalDate.now(),
            album = "Album #${index + startIndex}",
            lyrics = "lyrics #${index + startIndex}",
            links = TrackEntity.Links(
                artwork = "",
                spotify = "",
                youtube = "",
                soundCloud = "",
                appleMusic = "",
                musicBrainz = "",
                deezer = "",
                napster = "",
            ),
            metadata = TrackEntity.Metadata(
                lastRecognitionDate = Instant.ofEpochSecond(index.toLong()),
                isFavorite = favorites
            )
        )
    }
}