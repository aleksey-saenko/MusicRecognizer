package com.mrsep.musicrecognizer.feature.preferences.domain

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setApiToken(newToken: String)
    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setNotificationServiceEnabled(value: Boolean)
    suspend fun setDynamicColorsEnabled(value: Boolean)
    suspend fun setArtworkBasedThemeEnabled(value: Boolean)
    suspend fun setDeveloperModeEnabled(value: Boolean)
    suspend fun setRequiredServices(requiredServices: UserPreferences.RequiredServices)
    suspend fun setFallbackPolicy(fallbackPolicy: UserPreferences.FallbackPolicy)

}