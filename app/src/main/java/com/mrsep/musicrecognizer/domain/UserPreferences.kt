package com.mrsep.musicrecognizer.domain

import javax.annotation.concurrent.Immutable

@Immutable
data class UserPreferences(
    val onboardingCompleted: Boolean,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val developerModeEnabled: Boolean,
)