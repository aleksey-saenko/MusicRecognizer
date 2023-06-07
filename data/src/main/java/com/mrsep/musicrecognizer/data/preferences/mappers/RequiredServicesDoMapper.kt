package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto.RequiredServicesProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import javax.inject.Inject

class RequiredServicesDoMapper @Inject constructor() :
    BidirectionalMapper<RequiredServicesProto, RequiredServicesDo> {

    override fun map(input: RequiredServicesProto): RequiredServicesDo {
        return RequiredServicesDo(
            spotify = input.spotify,
            youtube = input.youtube,
            soundCloud = input.soundcloud,
            appleMusic = input.appleMusic,
            deezer = input.deezer,
            napster = input.napster,
            musicbrainz = input.musicbrainz
        )
    }

    override fun reverseMap(input: RequiredServicesDo): RequiredServicesProto {
        return RequiredServicesProto.newBuilder()
            .setSpotify(input.spotify)
            .setYoutube(input.youtube)
            .setSoundcloud(input.soundCloud)
            .setAppleMusic(input.appleMusic)
            .setDeezer(input.deezer)
            .setNapster(input.napster)
            .setMusicbrainz(input.musicbrainz)
            .build()
    }

}