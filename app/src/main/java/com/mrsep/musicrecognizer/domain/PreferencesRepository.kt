package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun saveApiToken(newToken: String)
    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setNotificationServiceEnabled(value: Boolean)
    suspend fun setDynamicColorsEnabled(value: Boolean)
    suspend fun setDeveloperModeEnabled(value: Boolean)
    suspend fun setVisibleLinks(visibleLinks: UserPreferences.VisibleLinks)

}