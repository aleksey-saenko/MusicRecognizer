package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FontSizeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.track.domain.model.FontSize
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import javax.inject.Inject

class LyricsFontStyleMapper @Inject constructor() :
    BidirectionalMapper<UserPreferencesDo.LyricsFontStyleDo, UserPreferences.LyricsFontStyle> {

    override fun map(input: UserPreferencesDo.LyricsFontStyleDo): UserPreferences.LyricsFontStyle {
        return UserPreferences.LyricsFontStyle(
            fontSize = when (input.fontSize) {
                FontSizeDo.Small -> FontSize.Small
                FontSizeDo.Normal -> FontSize.Normal
                FontSizeDo.Large -> FontSize.Large
                FontSizeDo.Huge -> FontSize.Huge
            },
            isBold = input.isBold,
            isHighContrast = input.isHighContrast,
            alignToStart = input.alignToStart,
        )
    }

    override fun reverseMap(input: UserPreferences.LyricsFontStyle): UserPreferencesDo.LyricsFontStyleDo {
        return UserPreferencesDo.LyricsFontStyleDo(
            fontSize = when (input.fontSize) {
                FontSize.Small -> FontSizeDo.Small
                FontSize.Normal -> FontSizeDo.Normal
                FontSize.Large -> FontSizeDo.Large
                FontSize.Huge -> FontSizeDo.Huge
            },
            isBold = input.isBold,
            isHighContrast = input.isHighContrast,
            alignToStart = input.alignToStart
        )
    }

}