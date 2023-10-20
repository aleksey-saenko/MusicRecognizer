package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.FallbackAction
import javax.inject.Inject

class FallbackActionMapper @Inject constructor(): Mapper<FallbackActionDo, FallbackAction> {

    override fun map(input: FallbackActionDo): FallbackAction {
        return when (input) {
            FallbackActionDo.Save -> FallbackAction.Save
            FallbackActionDo.SaveAndLaunch -> FallbackAction.SaveAndLaunch
            FallbackActionDo.Ignore -> FallbackAction.Ignore
        }
    }

}