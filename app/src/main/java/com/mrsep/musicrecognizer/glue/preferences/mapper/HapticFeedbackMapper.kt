package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import javax.inject.Inject

class HapticFeedbackMapper @Inject constructor() :
    BidirectionalMapper<UserPreferencesDo.HapticFeedbackDo, UserPreferences.HapticFeedback> {

    override fun map(input: UserPreferencesDo.HapticFeedbackDo): UserPreferences.HapticFeedback {
        return UserPreferences.HapticFeedback(
            vibrateOnTap = input.vibrateOnTap,
            vibrateOnResult = input.vibrateOnResult
        )
    }

    override fun reverseMap(input: UserPreferences.HapticFeedback): UserPreferencesDo.HapticFeedbackDo {
        return UserPreferencesDo.HapticFeedbackDo(
            vibrateOnTap = input.vibrateOnTap,
            vibrateOnResult = input.vibrateOnResult
        )
    }
}
