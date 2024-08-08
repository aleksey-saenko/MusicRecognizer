package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import javax.inject.Inject

internal class MusicServiceDoMapper @Inject constructor() :
    BidirectionalMapper<MusicServiceProto?, MusicServiceDo?> {

    override fun map(input: MusicServiceProto?): MusicServiceDo? {
        return when (input) {
            MusicServiceProto.AmazonMusic -> MusicServiceDo.AmazonMusic
            MusicServiceProto.Anghami -> MusicServiceDo.Anghami
            MusicServiceProto.AppleMusic -> MusicServiceDo.AppleMusic
            MusicServiceProto.Audiomack -> MusicServiceDo.Audiomack
            MusicServiceProto.Audius -> MusicServiceDo.Audius
            MusicServiceProto.Boomplay -> MusicServiceDo.Boomplay
            MusicServiceProto.Deezer -> MusicServiceDo.Deezer
            MusicServiceProto.MusicBrainz -> MusicServiceDo.MusicBrainz
            MusicServiceProto.Napster -> MusicServiceDo.Napster
            MusicServiceProto.Pandora -> MusicServiceDo.Pandora
            MusicServiceProto.Soundcloud -> MusicServiceDo.Soundcloud
            MusicServiceProto.Spotify -> MusicServiceDo.Spotify
            MusicServiceProto.Tidal -> MusicServiceDo.Tidal
            MusicServiceProto.YandexMusic -> MusicServiceDo.YandexMusic
            MusicServiceProto.Youtube -> MusicServiceDo.Youtube
            MusicServiceProto.YoutubeMusic -> MusicServiceDo.YoutubeMusic
            MusicServiceProto.UNRECOGNIZED -> null
            null -> null
        }
    }

    override fun reverseMap(input: MusicServiceDo?): MusicServiceProto? {
        return when (input) {
            MusicServiceDo.AmazonMusic -> MusicServiceProto.AmazonMusic
            MusicServiceDo.Anghami -> MusicServiceProto.Anghami
            MusicServiceDo.AppleMusic -> MusicServiceProto.AppleMusic
            MusicServiceDo.Audiomack -> MusicServiceProto.Audiomack
            MusicServiceDo.Audius -> MusicServiceProto.Audius
            MusicServiceDo.Boomplay -> MusicServiceProto.Boomplay
            MusicServiceDo.Deezer -> MusicServiceProto.Deezer
            MusicServiceDo.MusicBrainz -> MusicServiceProto.MusicBrainz
            MusicServiceDo.Napster -> MusicServiceProto.Napster
            MusicServiceDo.Pandora -> MusicServiceProto.Pandora
            MusicServiceDo.Soundcloud -> MusicServiceProto.Soundcloud
            MusicServiceDo.Spotify -> MusicServiceProto.Spotify
            MusicServiceDo.Tidal -> MusicServiceProto.Tidal
            MusicServiceDo.YandexMusic -> MusicServiceProto.YandexMusic
            MusicServiceDo.Youtube -> MusicServiceProto.Youtube
            MusicServiceDo.YoutubeMusic -> MusicServiceProto.YoutubeMusic
            null -> null
        }
    }
}
