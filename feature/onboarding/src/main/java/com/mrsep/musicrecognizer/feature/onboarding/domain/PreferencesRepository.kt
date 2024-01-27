package com.mrsep.musicrecognizer.feature.onboarding.domain

import com.mrsep.musicrecognizer.feature.onboarding.domain.model.AuddConfig
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setAuddConfig(value: AuddConfig)
    suspend fun setOnboardingCompleted(value: Boolean)

}