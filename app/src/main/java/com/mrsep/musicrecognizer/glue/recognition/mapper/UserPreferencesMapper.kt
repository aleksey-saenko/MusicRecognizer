package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.FallbackAction
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import javax.inject.Inject

class UserPreferencesMapper @Inject constructor(
    private val fallbackActionMapper: Mapper<FallbackActionDo, FallbackAction>
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
            fallbackPolicy = UserPreferences.FallbackPolicy(
                noMatches = fallbackActionMapper.map(input.fallbackPolicy.noMatches),
                badConnection = fallbackActionMapper.map(input.fallbackPolicy.badConnection),
                anotherFailure = fallbackActionMapper.map(input.fallbackPolicy.anotherFailure)
            ),
            hapticFeedback = UserPreferences.HapticFeedback(
                vibrateOnTap = input.hapticFeedback.vibrateOnTap,
                vibrateOnResult = input.hapticFeedback.vibrateOnResult
            )
        )
    }

}