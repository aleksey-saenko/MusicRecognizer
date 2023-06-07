package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.ScheduleActionDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduleAction
import javax.inject.Inject

class ScheduleActionMapper @Inject constructor(): Mapper<ScheduleActionDo, ScheduleAction> {

    override fun map(input: ScheduleActionDo): ScheduleAction {
        return when (input) {
            ScheduleActionDo.Save -> ScheduleAction.Save
            ScheduleActionDo.SaveAndLaunch -> ScheduleAction.SaveAndLaunch
            ScheduleActionDo.Ignore -> ScheduleAction.Ignore
        }
    }

}