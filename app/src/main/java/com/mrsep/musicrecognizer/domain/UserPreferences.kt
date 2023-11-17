package com.mrsep.musicrecognizer.domain

import javax.annotation.concurrent.Immutable

@Immutable
data class UserPreferences(
    val onboardingCompleted: Boolean,
    val notificationServiceEnabled: Boolean,
    val themeMode: ThemeMode,
    val dynamicColorsEnabled: Boolean,
    val usePureBlackForDarkTheme: Boolean,
    val developerModeEnabled: Boolean,
)

enum class ThemeMode { FollowSystem, AlwaysLight, AlwaysDark }