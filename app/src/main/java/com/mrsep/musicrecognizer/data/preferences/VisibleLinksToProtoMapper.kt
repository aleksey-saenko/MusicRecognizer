package com.mrsep.musicrecognizer.data.preferences

import com.mrsep.musicrecognizer.UserPreferencesProto.VisibleLinksProto
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import javax.inject.Inject

class VisibleLinksToProtoMapper @Inject constructor() :
    Mapper<UserPreferences.VisibleLinks, VisibleLinksProto> {

    override fun map(input: UserPreferences.VisibleLinks): VisibleLinksProto {
        return VisibleLinksProto.newBuilder()
            .setSpotify(input.spotify)
            .setAppleMusic(input.appleMusic)
            .setDeezer(input.deezer)
            .setNapster(input.napster)
            .setMusicbrainz(input.musicbrainz)
            .build()
    }

}