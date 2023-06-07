package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.data.preferences.ScheduleActionDo
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.feature.preferences.domain.ScheduleAction
import javax.inject.Inject

class ScheduleActionMapper @Inject constructor() :
    BidirectionalMapper<ScheduleActionDo, ScheduleAction> {

    override fun map(input: ScheduleActionDo): ScheduleAction {
        return when (input) {
            ScheduleActionDo.Ignore -> ScheduleAction.Ignore
            ScheduleActionDo.Save -> ScheduleAction.Save
            ScheduleActionDo.SaveAndLaunch -> ScheduleAction.SaveAndLaunch
        }
    }

    override fun reverseMap(input: ScheduleAction): ScheduleActionDo {
        return when (input) {
            ScheduleAction.Ignore -> ScheduleActionDo.Ignore
            ScheduleAction.Save -> ScheduleActionDo.Save
            ScheduleAction.SaveAndLaunch -> ScheduleActionDo.SaveAndLaunch
        }
    }

}