package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import javax.inject.Inject

class PreferencesMapper @Inject constructor(
    private val lyricsFontStyleMapper: BidirectionalMapper<UserPreferencesDo.LyricsFontStyleDo, UserPreferences.LyricsFontStyle>
) :
    Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            requiredServices = UserPreferences.RequiredServices(
                spotify = input.requiredServices.spotify,
                youtube = input.requiredServices.youtube,
                soundCloud = input.requiredServices.soundCloud,
                appleMusic = input.requiredServices.appleMusic,
                deezer = input.requiredServices.deezer,
                napster = input.requiredServices.napster,
                musicbrainz = input.requiredServices.musicbrainz
            ),
            lyricsFontStyle = lyricsFontStyleMapper.map(input.lyricsFontStyle),
            artworkBasedThemeEnabled = input.artworkBasedThemeEnabled
        )
    }

}