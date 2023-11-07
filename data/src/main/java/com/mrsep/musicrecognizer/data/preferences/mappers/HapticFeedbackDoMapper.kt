package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.UserPreferencesProto.HapticFeedbackProto
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.HapticFeedbackDo
import javax.inject.Inject

class HapticFeedbackDoMapper @Inject constructor() :
    BidirectionalMapper<HapticFeedbackProto, HapticFeedbackDo> {

    override fun map(input: HapticFeedbackProto): HapticFeedbackDo {
        return HapticFeedbackDo(
            vibrateOnTap = input.vibrateOnTap,
            vibrateOnResult = input.vibrateOnResult
        )
    }

    override fun reverseMap(input: HapticFeedbackDo): HapticFeedbackProto {
        return HapticFeedbackProto.newBuilder()
            .setVibrateOnTap(input.vibrateOnTap)
            .setVibrateOnResult(input.vibrateOnResult)
            .build()
    }

}