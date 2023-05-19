package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.UserPreferencesProto.ScheduleActionProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.feature.preferences.domain.ScheduleAction
import javax.inject.Inject

class ScheduleActionMapper @Inject constructor(): BidirectionalMapper<ScheduleActionProto, ScheduleAction> {

    override fun map(input: ScheduleActionProto): ScheduleAction {
        return when (input) {
            ScheduleActionProto.IGNORE -> ScheduleAction.Ignore
            ScheduleActionProto.SAVE -> ScheduleAction.Save
            ScheduleActionProto.SAVE_AND_LAUNCH -> ScheduleAction.SaveAndLaunch
            ScheduleActionProto.UNRECOGNIZED -> ScheduleAction.Ignore
        }
    }

    override fun reverseMap(input: ScheduleAction): ScheduleActionProto {
        return when (input) {
            ScheduleAction.Ignore -> ScheduleActionProto.IGNORE
            ScheduleAction.Save -> ScheduleActionProto.SAVE
            ScheduleAction.SaveAndLaunch -> ScheduleActionProto.SAVE_AND_LAUNCH
        }
    }

}