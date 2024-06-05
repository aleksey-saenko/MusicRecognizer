package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.feature.preferences.domain.FallbackAction
import javax.inject.Inject

class FallbackActionMapper @Inject constructor() :
    BidirectionalMapper<FallbackActionDo, FallbackAction> {

    override fun map(input: FallbackActionDo): FallbackAction {
        return when (input) {
            FallbackActionDo.Ignore -> FallbackAction.Ignore
            FallbackActionDo.Save -> FallbackAction.Save
            FallbackActionDo.SaveAndLaunch -> FallbackAction.SaveAndLaunch
        }
    }

    override fun reverseMap(input: FallbackAction): FallbackActionDo {
        return when (input) {
            FallbackAction.Ignore -> FallbackActionDo.Ignore
            FallbackAction.Save -> FallbackActionDo.Save
            FallbackAction.SaveAndLaunch -> FallbackActionDo.SaveAndLaunch
        }
    }
}
