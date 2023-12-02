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
                    spotify?.run { TrackLink(this, MusicService.Spotify) },
                    youtube?.run { TrackLink(this, MusicService.Youtube) },
                    soundCloud?.run { TrackLink(this, MusicService.Soundcloud) },
                    appleMusic?.run { TrackLink(this, MusicService.AppleMusic) },
                    musicBrainz?.run { TrackLink(this, MusicService.MusicBrainz) },
                    deezer?.run { TrackLink(this, MusicService.Deezer) },
                    napster?.run { TrackLink(this, MusicService.Napster) },
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
                    spotify = linkMap[MusicService.Spotify],
                    youtube = linkMap[MusicService.Youtube],
                    soundCloud = linkMap[MusicService.Soundcloud],
                    appleMusic = linkMap[MusicService.AppleMusic],
                    musicBrainz = linkMap[MusicService.MusicBrainz],
                    deezer = linkMap[MusicService.Deezer],
                    napster = linkMap[MusicService.Napster]
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