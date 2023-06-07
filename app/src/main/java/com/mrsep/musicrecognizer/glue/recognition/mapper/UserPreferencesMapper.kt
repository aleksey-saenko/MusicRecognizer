package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.ScheduleActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduleAction
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import javax.inject.Inject

class UserPreferencesMapper @Inject constructor(
    private val scheduleActionMapper: Mapper<ScheduleActionDo, ScheduleAction>
) :
    Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            apiToken = input.apiToken,
            notificationServiceEnabled = input.notificationServiceEnabled,
            dynamicColorsEnabled = input.dynamicColorsEnabled,
            developerModeEnabled = input.developerModeEnabled,
            requiredServices = UserPreferences.RequiredServices(
                spotify = input.requiredServices.spotify,
                youtube = input.requiredServices.youtube,
                soundCloud = input.requiredServices.soundCloud,
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