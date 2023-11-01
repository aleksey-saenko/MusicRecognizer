package com.mrsep.musicrecognizer.data.track.util

import com.mrsep.musicrecognizer.data.track.TrackEntity
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

internal fun getFakeTrackList(startIndex: Int, endIndex: Int, inclusive: Boolean, favorites: Boolean): List<TrackEntity> {
    return List(if (inclusive) endIndex - startIndex + 1 else endIndex - startIndex) { index ->
        TrackEntity(
            mbId = UUID.randomUUID().toString(),
            title = "Track #${index + startIndex}",
            artist = "Artist #${index + startIndex}",
            releaseDate = LocalDate.now(),
            album = "Album #${index + startIndex}",
            lyrics = "lyrics #${index + startIndex}",
            links = TrackEntity.Links(
//                artwork = "https://upload.wikimedia.org/wikipedia/ru/1/1e/Meddle_album_cover.jpg",
                artwork = "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png",
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
                isFavorite = favorites,
                themeSeedColor = null
            )
        )
    }
}

internal val fakeTrack by lazy {
    TrackEntity(
        mbId = UUID.randomUUID().toString(),
        title = "Echoes",
        artist = "Pink Floyd",
        album = "Meddle",
        releaseDate = LocalDate.parse("1971-10-30", DateTimeFormatter.ISO_DATE),
        lyrics = "lyrics stub",
        links = TrackEntity.Links(
            artwork = "https://upload.wikimedia.org/wikipedia/ru/1/1e/Meddle_album_cover.jpg",
            spotify = null,
            youtube = null,
            soundCloud = null,
            appleMusic = null,
            musicBrainz = null,
            deezer = null,
            napster = null
        ),
        metadata = TrackEntity.Metadata(
            lastRecognitionDate = Instant.now(),
            isFavorite = false,
            themeSeedColor = null
        )
    )
}