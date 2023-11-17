package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.feature.preferences.domain.ThemeMode
import javax.inject.Inject

class ThemeModeMapper @Inject constructor():
    BidirectionalMapper<ThemeModeDo, ThemeMode> {

    override fun map(input: ThemeModeDo): ThemeMode {
        return when (input) {
            ThemeModeDo.FollowSystem -> ThemeMode.FollowSystem
            ThemeModeDo.AlwaysLight -> ThemeMode.AlwaysLight
            ThemeModeDo.AlwaysDark -> ThemeMode.AlwaysDark
        }
    }

    override fun reverseMap(input: ThemeMode): ThemeModeDo {
        return when (input) {
            ThemeMode.FollowSystem -> ThemeModeDo.FollowSystem
            ThemeMode.AlwaysLight -> ThemeModeDo.AlwaysLight
            ThemeMode.AlwaysDark -> ThemeModeDo.AlwaysDark
        }
    }

}