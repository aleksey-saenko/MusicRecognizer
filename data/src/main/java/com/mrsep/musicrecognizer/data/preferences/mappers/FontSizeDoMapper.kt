package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto.FontSizeProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FontSizeDo
import javax.inject.Inject

class FontSizeDoMapper @Inject constructor(): BidirectionalMapper<FontSizeProto, FontSizeDo> {

    override fun map(input: FontSizeProto): FontSizeDo {
        return when (input) {
            FontSizeProto.SMALL -> FontSizeDo.Small
            FontSizeProto.NORMAL -> FontSizeDo.Normal
            FontSizeProto.LARGE -> FontSizeDo.Large
            FontSizeProto.HUGE -> FontSizeDo.Huge
            FontSizeProto.UNRECOGNIZED -> FontSizeDo.Normal
        }
    }

    override fun reverseMap(input: FontSizeDo): FontSizeProto {
        return when (input) {
            FontSizeDo.Small -> FontSizeProto.SMALL
            FontSizeDo.Normal -> FontSizeProto.NORMAL
            FontSizeDo.Large -> FontSizeProto.LARGE
            FontSizeDo.Huge -> FontSizeProto.HUGE
        }
    }

}