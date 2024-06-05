package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.LyricsFontStyleProto
import com.mrsep.musicrecognizer.UserPreferencesProtoKt.lyricsFontStyleProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FontSizeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import javax.inject.Inject

class LyricsFontStyleDoMapper @Inject constructor(
    private val fontSizeMapper: BidirectionalMapper<UserPreferencesProto.FontSizeProto, FontSizeDo>
) : BidirectionalMapper<LyricsFontStyleProto, UserPreferencesDo.LyricsFontStyleDo> {

    override fun map(input: LyricsFontStyleProto): UserPreferencesDo.LyricsFontStyleDo {
        return UserPreferencesDo.LyricsFontStyleDo(
            fontSize = fontSizeMapper.map(input.fontSize),
            isBold = input.isBold,
            isHighContrast = input.isHighContrast,
            alignToStart = input.alignToStart
        )
    }

    override fun reverseMap(input: UserPreferencesDo.LyricsFontStyleDo): LyricsFontStyleProto {
        return lyricsFontStyleProto {
            fontSize = fontSizeMapper.reverseMap(input.fontSize)
            isBold = input.isBold
            isHighContrast = input.isHighContrast
            alignToStart = input.alignToStart
        }
    }
}
