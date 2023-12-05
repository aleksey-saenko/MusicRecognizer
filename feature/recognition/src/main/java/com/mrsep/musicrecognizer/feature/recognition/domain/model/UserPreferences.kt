package com.mrsep.musicrecognizer.feature.recognition.domain.model

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val apiToken: String,
    val requiredMusicServices: List<MusicService>,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val developerModeEnabled: Boolean,
    val fallbackPolicy: FallbackPolicy,
    val hapticFeedback: HapticFeedback
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

    Ignore(false, false),
    Save(true, false),
    SaveAndLaunch(true, true);

    operator fun component1() = save
    operator fun component2() = launch

}