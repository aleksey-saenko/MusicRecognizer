package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import javax.inject.Inject

class RequiredServicesMapper @Inject constructor() :
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