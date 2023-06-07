package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.RequiredServicesDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences.RequiredServices
import javax.inject.Inject

class RequiredServicesMapper @Inject constructor() :
    BidirectionalMapper<RequiredServicesDo, RequiredServices> {

    override fun map(input: RequiredServicesDo): RequiredServices {
        return RequiredServices(
            spotify = input.spotify,
            youtube = input.youtube,
            soundCloud = input.soundCloud,
            appleMusic = input.appleMusic,
            deezer = input.deezer,
            napster = input.napster,
            musicbrainz = input.musicbrainz
        )
    }

    override fun reverseMap(input: RequiredServices): RequiredServicesDo {
        return RequiredServicesDo(
            spotify = input.spotify,
            youtube = input.youtube,
            soundCloud = input.soundCloud,
            appleMusic = input.appleMusic,
            deezer = input.deezer,
            napster = input.napster,
            musicbrainz = input.musicbrainz
        )
    }

}