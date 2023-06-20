package com.mrsep.musicrecognizer.feature.onboarding.domain.model

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val apiToken: String
)