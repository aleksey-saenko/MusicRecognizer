package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto.ScheduleActionProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.ScheduleActionDo
import javax.inject.Inject

class ScheduleActionDoMapper @Inject constructor():
    BidirectionalMapper<ScheduleActionProto, ScheduleActionDo> {

    override fun map(input: ScheduleActionProto): ScheduleActionDo {
        return when (input) {
            ScheduleActionProto.IGNORE -> ScheduleActionDo.Ignore
            ScheduleActionProto.SAVE -> ScheduleActionDo.Save
            ScheduleActionProto.SAVE_AND_LAUNCH -> ScheduleActionDo.SaveAndLaunch
            ScheduleActionProto.UNRECOGNIZED -> ScheduleActionDo.Ignore
        }
    }

    override fun reverseMap(input: ScheduleActionDo): ScheduleActionProto {
        return when (input) {
            ScheduleActionDo.Ignore -> ScheduleActionProto.IGNORE
            ScheduleActionDo.Save -> ScheduleActionProto.SAVE
            ScheduleActionDo.SaveAndLaunch -> ScheduleActionProto.SAVE_AND_LAUNCH
        }
    }

}