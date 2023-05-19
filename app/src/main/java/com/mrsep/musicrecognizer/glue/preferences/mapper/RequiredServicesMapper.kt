package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.UserPreferencesProto.RequiredServicesProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.RequiredServices
import javax.inject.Inject

class RequiredServicesMapper @Inject constructor() :
    BidirectionalMapper<RequiredServicesProto, RequiredServices> {

    override fun map(input: RequiredServicesProto): RequiredServices {
        return RequiredServices(
            spotify = input.spotify,
            youtube = input.youtube,
            soundCloud = input.soundcloud,
            appleMusic = input.appleMusic,
            deezer = input.deezer,
            napster = input.napster,
            musicbrainz = input.musicbrainz
        )
    }

    override fun reverseMap(input: RequiredServices): RequiredServicesProto {
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