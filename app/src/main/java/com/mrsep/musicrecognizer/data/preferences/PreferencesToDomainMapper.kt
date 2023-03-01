package com.mrsep.musicrecognizer.data.preferences

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import javax.inject.Inject

class PreferencesToDomainMapper @Inject constructor() :
    Mapper<UserPreferencesProto, UserPreferences> {

    override fun map(input: UserPreferencesProto): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            apiToken = input.apiToken,
            notificationServiceEnabled = input.notificationServiceEnabled,
            dynamicColorsEnabled = input.dynamicColorsEnabled,
            visibleLinks = UserPreferences.VisibleLinks(
                spotify = input.visibleLinks.spotify,
                appleMusic = input.visibleLinks.appleMusic,
                deezer = input.visibleLinks.deezer,
                napster = input.visibleLinks.napster,
                musicbrainz = input.visibleLinks.musicbrainz
            )
        )
    }

}