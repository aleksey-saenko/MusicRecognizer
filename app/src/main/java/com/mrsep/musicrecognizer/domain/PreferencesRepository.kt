package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun saveApiToken(newToken: String)
    suspend fun setOnboardingCompleted(onboardingCompleted: Boolean)
    suspend fun setVisibleLinks(visibleLinks: UserPreferences.VisibleLinks)

}