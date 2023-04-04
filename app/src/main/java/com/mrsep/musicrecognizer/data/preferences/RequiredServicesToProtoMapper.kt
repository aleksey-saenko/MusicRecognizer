package com.mrsep.musicrecognizer.data.preferences

import com.mrsep.musicrecognizer.UserPreferencesProto.RequiredServicesProto
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import javax.inject.Inject

class RequiredServicesToProtoMapper @Inject constructor() :
    Mapper<UserPreferences.RequiredServices, RequiredServicesProto> {

    override fun map(input: UserPreferences.RequiredServices): RequiredServicesProto {
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