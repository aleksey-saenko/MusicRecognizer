package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.TrackLink
import javax.inject.Inject

class TrackMapper @Inject constructor() : BidirectionalMapper<TrackEntity, Track> {

    override fun map(input: TrackEntity): Track {
        return Track(
            mbId = input.mbId,
            title = input.title,
            artist = input.artist,
            album = input.album,
            releaseDate = input.releaseDate,
            lyrics = input.lyrics,
            artworkUrl = input.links.artwork,
            trackLinks = with(input.links) {
                listOfNotNull(
                    amazonMusic?.run { TrackLink(this, MusicService.AmazonMusic) },
                    anghami?.run { TrackLink(this, MusicService.Anghami) },
                    appleMusic?.run { TrackLink(this, MusicService.AppleMusic) },
                    audiomack?.run { TrackLink(this, MusicService.Audiomack) },
                    audius?.run { TrackLink(this, MusicService.Audius) },
                    boomplay?.run { TrackLink(this, MusicService.Boomplay) },
                    deezer?.run { TrackLink(this, MusicService.Deezer) },
                    musicBrainz?.run { TrackLink(this, MusicService.MusicBrainz) },
                    napster?.run { TrackLink(this, MusicService.Napster) },
                    pandora?.run { TrackLink(this, MusicService.Pandora) },
                    soundCloud?.run { TrackLink(this, MusicService.Soundcloud) },
                    spotify?.run { TrackLink(this, MusicService.Spotify) },
                    tidal?.run { TrackLink(this, MusicService.Tidal) },
                    yandexMusic?.run { TrackLink(this, MusicService.YandexMusic) },
                    youtube?.run { TrackLink(this, MusicService.Youtube) },
                    youtubeMusic?.run { TrackLink(this, MusicService.YoutubeMusic) },
                )
            },
            metadata = Track.Metadata(
                lastRecognitionDate = input.metadata.lastRecognitionDate,
                isFavorite = input.metadata.isFavorite,
                themeSeedColor = input.metadata.themeSeedColor
            )
        )
    }

    override fun reverseMap(input: Track): TrackEntity {
        return TrackEntity(
            mbId = input.mbId,
            title = input.title,
            artist = input.artist,
            album = input.album,
            releaseDate = input.releaseDate,
            lyrics = input.lyrics,
            links = run {
                val linkMap = input.trackLinks.associate { link -> link.service to link.url }
                TrackEntity.Links(
                    artwork = input.artworkUrl,
                    amazonMusic = linkMap[MusicService.AmazonMusic],
                    anghami = linkMap[MusicService.Anghami],
                    appleMusic = linkMap[MusicService.AppleMusic],
                    audiomack = linkMap[MusicService.Audiomack],
                    audius = linkMap[MusicService.Audius],
                    boomplay = linkMap[MusicService.Boomplay],
                    deezer = linkMap[MusicService.Deezer],
                    musicBrainz = linkMap[MusicService.MusicBrainz],
                    napster = linkMap[MusicService.Napster],
                    pandora = linkMap[MusicService.Pandora],
                    soundCloud = linkMap[MusicService.Soundcloud],
                    spotify = linkMap[MusicService.Spotify],
                    tidal = linkMap[MusicService.Tidal],
                    yandexMusic = linkMap[MusicService.YandexMusic],
                    youtube = linkMap[MusicService.Youtube],
                    youtubeMusic = linkMap[MusicService.YoutubeMusic],
                )
            },
            metadata = TrackEntity.Metadata(
                lastRecognitionDate = input.metadata.lastRecognitionDate,
                isFavorite = input.metadata.isFavorite,
                themeSeedColor = input.metadata.themeSeedColor
            )
        )
    }

}