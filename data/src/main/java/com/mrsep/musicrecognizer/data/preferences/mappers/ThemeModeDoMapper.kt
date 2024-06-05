package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto.ThemeModeProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import javax.inject.Inject

class ThemeModeDoMapper @Inject constructor() :
    BidirectionalMapper<ThemeModeProto, ThemeModeDo> {

    override fun map(input: ThemeModeProto): ThemeModeDo {
        return when (input) {
            ThemeModeProto.FOLLOW_SYSTEM -> ThemeModeDo.FollowSystem
            ThemeModeProto.ALWAYS_LIGHT -> ThemeModeDo.AlwaysLight
            ThemeModeProto.ALWAYS_DARK -> ThemeModeDo.AlwaysDark
            ThemeModeProto.UNRECOGNIZED -> ThemeModeDo.FollowSystem
        }
    }

    override fun reverseMap(input: ThemeModeDo): ThemeModeProto {
        return when (input) {
            ThemeModeDo.FollowSystem -> ThemeModeProto.FOLLOW_SYSTEM
            ThemeModeDo.AlwaysLight -> ThemeModeProto.ALWAYS_LIGHT
            ThemeModeDo.AlwaysDark -> ThemeModeProto.ALWAYS_DARK
        }
    }
}
