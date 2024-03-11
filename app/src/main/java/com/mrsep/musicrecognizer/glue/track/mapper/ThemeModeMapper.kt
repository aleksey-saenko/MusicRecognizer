package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
import javax.inject.Inject

class ThemeModeMapper @Inject constructor():
    Mapper<ThemeModeDo, ThemeMode> {

    override fun map(input: ThemeModeDo): ThemeMode {
        return when (input) {
            ThemeModeDo.FollowSystem -> ThemeMode.FollowSystem
            ThemeModeDo.AlwaysLight -> ThemeMode.AlwaysLight
            ThemeModeDo.AlwaysDark -> ThemeMode.AlwaysDark
        }
    }

}