package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import javax.inject.Inject

class MusicServiceMapper @Inject constructor() :
    Mapper<MusicServiceDo, MusicService> {

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