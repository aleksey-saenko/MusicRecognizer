package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.feature.preferences.domain.MusicService
import javax.inject.Inject

class MusicServiceMapper @Inject constructor() :
    BidirectionalMapper<MusicServiceDo, MusicService> {

    override fun reverseMap(input: MusicService): MusicServiceDo {
        return when (input) {
            MusicService.AmazonMusic -> MusicServiceDo.AmazonMusic
            MusicService.Anghami -> MusicServiceDo.Anghami
            MusicService.AppleMusic -> MusicServiceDo.AppleMusic
            MusicService.Audiomack -> MusicServiceDo.Audiomack
            MusicService.Audius -> MusicServiceDo.Audius
            MusicService.Boomplay -> MusicServiceDo.Boomplay
            MusicService.Deezer -> MusicServiceDo.Deezer
            MusicService.MusicBrainz -> MusicServiceDo.MusicBrainz
            MusicService.Napster -> MusicServiceDo.Napster
            MusicService.Pandora -> MusicServiceDo.Pandora
            MusicService.Soundcloud -> MusicServiceDo.Soundcloud
            MusicService.Spotify -> MusicServiceDo.Spotify
            MusicService.Tidal -> MusicServiceDo.Tidal
            MusicService.YandexMusic -> MusicServiceDo.YandexMusic
            MusicService.Youtube -> MusicServiceDo.Youtube
            MusicService.YoutubeMusic -> MusicServiceDo.YoutubeMusic
        }
    }

    override fun map(input: MusicServiceDo): MusicService {
        return when (input) {
            MusicServiceDo.AmazonMusic -> MusicService.AmazonMusic
            MusicServiceDo.Anghami -> MusicService.Anghami
            MusicServiceDo.AppleMusic -> MusicService.AppleMusic
            MusicServiceDo.Audiomack -> MusicService.Audiomack
            MusicServiceDo.Audius -> MusicService.Audius
            MusicServiceDo.Boomplay -> MusicService.Boomplay
            MusicServiceDo.Deezer -> MusicService.Deezer
            MusicServiceDo.MusicBrainz -> MusicService.MusicBrainz
            MusicServiceDo.Napster -> MusicService.Napster
            MusicServiceDo.Pandora -> MusicService.Pandora
            MusicServiceDo.Soundcloud -> MusicService.Soundcloud
            MusicServiceDo.Spotify -> MusicService.Spotify
            MusicServiceDo.Tidal -> MusicService.Tidal
            MusicServiceDo.YandexMusic -> MusicService.YandexMusic
            MusicServiceDo.Youtube -> MusicService.Youtube
            MusicServiceDo.YoutubeMusic -> MusicService.YoutubeMusic
        }
    }
}
