package com.mrsep.musicrecognizer.feature.onboarding.domain.model

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val apiToken: String,
    val requiredServices: RequiredServices,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val developerModeEnabled: Boolean,
) {

    data class RequiredServices(
        val spotify: Boolean,
        val youtube: Boolean,
        val soundCloud: Boolean,
        val appleMusic: Boolean,
        val deezer: Boolean,
        val napster: Boolean,
        val musicbrainz: Boolean
    )

}