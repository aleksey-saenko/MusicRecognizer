package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import javax.inject.Inject

class RequiredServicesToProtoMapper @Inject constructor() :
    Mapper<UserPreferences.RequiredServices, UserPreferencesProto.RequiredServicesProto> {

    override fun map(input: UserPreferences.RequiredServices): UserPreferencesProto.RequiredServicesProto {
        return UserPreferencesProto.RequiredServicesProto.newBuilder()
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