package com.mrsep.musicrecognizer.glue.onboarding.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.UserPreferences
import javax.inject.Inject

class PreferencesMapper @Inject constructor() : Mapper<UserPreferencesDo, UserPreferences> {

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
            )
        )
    }

}