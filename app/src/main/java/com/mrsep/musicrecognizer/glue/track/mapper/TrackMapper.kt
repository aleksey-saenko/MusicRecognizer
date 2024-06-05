package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.TrackLink
import javax.inject.Inject

class TrackMapper @Inject constructor() : Mapper<TrackEntity, Track> {

    override fun map(input: TrackEntity): Track {
        return Track(
            id = input.id,
            title = input.title,
            artist = input.artist,
            album = input.album,
            releaseDate = input.releaseDate,
            duration = input.duration,
            recognizedAt = input.recognizedAt,
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
            themeSeedColor = input.properties.themeSeedColor,
            recognitionDate = input.recognitionDate,
            isViewed = input.properties.isViewed,
            isFavorite = input.properties.isFavorite,
        )
    }
}
