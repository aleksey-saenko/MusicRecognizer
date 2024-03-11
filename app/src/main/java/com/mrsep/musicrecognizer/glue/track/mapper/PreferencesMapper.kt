package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences.*
import javax.inject.Inject

class PreferencesMapper @Inject constructor(
    private val musicServiceMapper: Mapper<MusicServiceDo, MusicService>,
    private val lyricsFontStyleMapper: BidirectionalMapper<LyricsFontStyleDo, LyricsFontStyle>,
    private val themeModeMapper: Mapper<ThemeModeDo, ThemeMode>,
) : Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            requiredMusicServices = input.requiredMusicServices
                .map(musicServiceMapper::map),
            lyricsFontStyle = lyricsFontStyleMapper.map(input.lyricsFontStyle),
            artworkBasedThemeEnabled = input.artworkBasedThemeEnabled,
            themeMode = themeModeMapper.map(input.themeMode)
        )
    }

}