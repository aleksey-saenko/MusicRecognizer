package com.mrsep.musicrecognizer.data.track.util

import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

internal fun getFakeTrackList(
    startIndex: Int,
    endIndex: Int,
    inclusive: Boolean,
    favorites: Boolean
): List<TrackEntity> {
    return List(if (inclusive) endIndex - startIndex + 1 else endIndex - startIndex) { index ->
        TrackEntity(
            id = UUID.randomUUID().toString(),
            title = "Track #${index + startIndex}",
            artist = "Artist #${index + startIndex}",
            releaseDate = LocalDate.now()
                .minus(index * 10L, ChronoUnit.DAYS),
            duration = Duration.ofSeconds(210),
            recognizedAt = Duration.ofSeconds(157),
            recognizedBy = RecognitionProviderDo.entries.random(),
            recognitionDate = Instant.now().minus(index * index * 10L, ChronoUnit.HOURS),
            album = "Album #${index + startIndex}",
            lyrics = "lyrics #${index + startIndex}",
            links = TrackEntity.Links(
                artwork = "https://upload.wikimedia.org/wikipedia/ru/4/46/Obscured_by_Clouds_Pink_Floyd.jpg",
                amazonMusic = "",
                anghami = "",
                appleMusic = "",
                audiomack = "",
                audius = "",
                boomplay = "",
                deezer = "",
                musicBrainz = "",
                napster = "",
                pandora = "",
                soundCloud = "",
                spotify = "",
                tidal = "",
                yandexMusic = "",
                youtube = "",
                youtubeMusic = "",
            ),
            properties = TrackEntity.Properties(
                isFavorite = favorites,
                isViewed = true,
                themeSeedColor = null,
            ),
        )
    }
}
