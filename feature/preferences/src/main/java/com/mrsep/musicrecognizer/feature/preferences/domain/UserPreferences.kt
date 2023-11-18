package com.mrsep.musicrecognizer.feature.preferences.domain

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val apiToken: String,
    val requiredServices: RequiredServices,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val artworkBasedThemeEnabled: Boolean,
    val developerModeEnabled: Boolean,
    val fallbackPolicy: FallbackPolicy,
    val hapticFeedback: HapticFeedback,
    val useColumnForLibrary: Boolean,
    val themeMode: ThemeMode,
    val usePureBlackForDarkTheme: Boolean,
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

    data class FallbackPolicy(
        val noMatches: FallbackAction,
        val badConnection: FallbackAction,
        val anotherFailure: FallbackAction
    )

    data class HapticFeedback(
        val vibrateOnTap: Boolean,
        val vibrateOnResult: Boolean,
    )

}

enum class FallbackAction(val save: Boolean, val launch: Boolean) {
    Ignore(false, false), Save(true, false), SaveAndLaunch(true, true);

    operator fun component1() = save
    operator fun component2() = launch
}

enum class ThemeMode { FollowSystem, AlwaysLight, AlwaysDark }