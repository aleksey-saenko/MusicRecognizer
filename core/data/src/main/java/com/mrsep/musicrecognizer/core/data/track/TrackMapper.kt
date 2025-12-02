package com.mrsep.musicrecognizer.core.data.track

import com.mrsep.musicrecognizer.core.database.track.TrackEntity
import com.mrsep.musicrecognizer.core.domain.track.model.Lyrics
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.PlainLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.SyncedLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.recognition.lyrics.LyricsConverter
import com.mrsep.musicrecognizer.core.recognition.lyrics.SyncedLyricsWithMetadata

internal fun Track.toEntity() = TrackEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    releaseDate = releaseDate,
    duration = duration,
    recognizedAt = recognizedAt,
    recognizedBy = recognizedBy,
    recognitionDate = recognitionDate,
    lyrics = lyrics?.toDbLyricsData(),
    isLyricsSynced = when (lyrics) {
        is SyncedLyrics -> true
        is PlainLyrics,
        null -> false
    },
    links = TrackEntity.Links(
        artworkThumbnail = artworkThumbUrl,
        artwork = artworkUrl,
        amazonMusic = trackLinks[MusicService.AmazonMusic],
        anghami = trackLinks[MusicService.Anghami],
        appleMusic = trackLinks[MusicService.AppleMusic],
        audiomack = trackLinks[MusicService.Audiomack],
        audius = trackLinks[MusicService.Audius],
        boomplay = trackLinks[MusicService.Boomplay],
        deezer = trackLinks[MusicService.Deezer],
        musicBrainz = trackLinks[MusicService.MusicBrainz],
        napster = trackLinks[MusicService.Napster],
        pandora = trackLinks[MusicService.Pandora],
        soundCloud = trackLinks[MusicService.Soundcloud],
        spotify = trackLinks[MusicService.Spotify],
        tidal = trackLinks[MusicService.Tidal],
        yandexMusic = trackLinks[MusicService.YandexMusic],
        youtube = trackLinks[MusicService.Youtube],
        youtubeMusic = trackLinks[MusicService.YoutubeMusic],
    ),
    properties = TrackEntity.Properties(
        isFavorite = properties.isFavorite,
        isViewed = properties.isViewed,
        themeSeedColor = properties.themeSeedColor,
    ),
)

internal fun TrackEntity.toDomain() = Track(
    id = id,
    title = title,
    artist = artist,
    album = album,
    releaseDate = releaseDate,
    duration = duration,
    recognizedAt = recognizedAt,
    recognizedBy = recognizedBy,
    recognitionDate = recognitionDate,
    lyrics = lyrics?.let { parseDbLyricsData(it, isLyricsSynced) },
    artworkThumbUrl = links.artworkThumbnail,
    artworkUrl = links.artwork,
    trackLinks = with(links) {
        buildMap {
            amazonMusic?.run { put(MusicService.AmazonMusic, this) }
            anghami?.run { put(MusicService.Anghami, this) }
            appleMusic?.run { put(MusicService.AppleMusic, this) }
            audiomack?.run { put(MusicService.Audiomack, this) }
            audius?.run { put(MusicService.Audius, this) }
            boomplay?.run { put(MusicService.Boomplay, this) }
            deezer?.run { put(MusicService.Deezer, this) }
            musicBrainz?.run { put(MusicService.MusicBrainz, this) }
            napster?.run { put(MusicService.Napster, this) }
            pandora?.run { put(MusicService.Pandora, this) }
            soundCloud?.run { put(MusicService.Soundcloud, this) }
            spotify?.run { put(MusicService.Spotify, this) }
            tidal?.run { put(MusicService.Tidal, this) }
            yandexMusic?.run { put(MusicService.YandexMusic, this) }
            youtube?.run { put(MusicService.Youtube, this) }
            youtubeMusic?.run { put(MusicService.YoutubeMusic, this) }
        }
    },
    properties = Track.Properties(
        isFavorite = properties.isFavorite,
        isViewed = properties.isViewed,
        themeSeedColor = properties.themeSeedColor,
    )
)

internal fun Lyrics.toDbLyricsData() = when (this) {
    is SyncedLyrics -> LyricsConverter().formatToString(
        lyrics = SyncedLyricsWithMetadata(metadata = emptyMap(), lines = lines),
        formatMetadata = false
    )
    is PlainLyrics -> plain
}

internal fun parseDbLyricsData(data: String, isSynced: Boolean): Lyrics? = if (isSynced) {
    data.let { LyricsConverter().parseFromString(input = it, parseMetadata = false) }
        ?.lines?.run(::SyncedLyrics)
} else {
    data.run(::PlainLyrics)
}
