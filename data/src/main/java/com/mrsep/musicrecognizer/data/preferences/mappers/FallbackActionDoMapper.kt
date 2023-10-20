package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto.FallbackActionProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import javax.inject.Inject

class FallbackActionDoMapper @Inject constructor():
    BidirectionalMapper<FallbackActionProto, FallbackActionDo> {

    override fun map(input: FallbackActionProto): FallbackActionDo {
        return when (input) {
            FallbackActionProto.IGNORE -> FallbackActionDo.Ignore
            FallbackActionProto.SAVE -> FallbackActionDo.Save
            FallbackActionProto.SAVE_AND_LAUNCH -> FallbackActionDo.SaveAndLaunch
            FallbackActionProto.UNRECOGNIZED -> FallbackActionDo.Ignore
        }
    }

    override fun reverseMap(input: FallbackActionDo): FallbackActionProto {
        return when (input) {
            FallbackActionDo.Ignore -> FallbackActionProto.IGNORE
            FallbackActionDo.Save -> FallbackActionProto.SAVE
            FallbackActionDo.SaveAndLaunch -> FallbackActionProto.SAVE_AND_LAUNCH
        }
    }

}