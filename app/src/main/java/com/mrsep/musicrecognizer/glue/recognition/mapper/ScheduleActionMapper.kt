package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.UserPreferencesProto.ScheduleActionProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduleAction
import javax.inject.Inject

class ScheduleActionMapper @Inject constructor(): Mapper<ScheduleActionProto, ScheduleAction> {

    override fun map(input: ScheduleActionProto): ScheduleAction {
        return when (input) {
            ScheduleActionProto.IGNORE -> ScheduleAction.Ignore
            ScheduleActionProto.SAVE -> ScheduleAction.Save
            ScheduleActionProto.SAVE_AND_LAUNCH -> ScheduleAction.SaveAndLaunch
            ScheduleActionProto.UNRECOGNIZED -> ScheduleAction.Ignore
        }
    }

}