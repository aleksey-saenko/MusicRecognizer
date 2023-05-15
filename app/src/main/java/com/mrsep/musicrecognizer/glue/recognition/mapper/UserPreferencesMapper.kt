package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.ScheduleActionProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduleAction
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import javax.inject.Inject

class UserPreferencesMapper @Inject constructor(
    private val scheduleActionMapper: Mapper<ScheduleActionProto, ScheduleAction>
) :
    Mapper<UserPreferencesProto, UserPreferences> {

    override fun map(input: UserPreferencesProto): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            apiToken = input.apiToken,
            notificationServiceEnabled = input.notificationServiceEnabled,
            dynamicColorsEnabled = input.dynamicColorsEnabled,
            developerModeEnabled = input.developerModeEnabled,
            requiredServices = UserPreferences.RequiredServices(
                spotify = input.requiredServices.spotify,
                youtube = input.requiredServices.youtube,
                soundCloud = input.requiredServices.soundcloud,
                appleMusic = input.requiredServices.appleMusic,
                deezer = input.requiredServices.deezer,
                napster = input.requiredServices.napster,
                musicbrainz = input.requiredServices.musicbrainz
            ),
            schedulePolicy = UserPreferences.SchedulePolicy(
                noMatches = scheduleActionMapper.map(input.schedulePolicy.noMatches),
                badConnection = scheduleActionMapper.map(input.schedulePolicy.badConnection),
                anotherFailure = scheduleActionMapper.map(input.schedulePolicy.anotherFailure)
            )
        )
    }

}