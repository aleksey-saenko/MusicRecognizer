package com.mrsep.musicrecognizer.feature.preferences.domain

import kotlinx.collections.immutable.ImmutableList

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val currentRecognitionProvider: RecognitionProvider,
    val auddConfig: AuddConfig,
    val acrCloudConfig: AcrCloudConfig,
    val requiredMusicServices: ImmutableList<MusicService>,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val artworkBasedThemeEnabled: Boolean,
    val developerModeEnabled: Boolean,
    val fallbackPolicy: FallbackPolicy,
    val hapticFeedback: HapticFeedback,
    val useGridForLibrary: Boolean,
    val useGridForRecognitionQueue: Boolean,
    val themeMode: ThemeMode,
    val usePureBlackForDarkTheme: Boolean,
    val recognizeOnStartup: Boolean,
) {

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