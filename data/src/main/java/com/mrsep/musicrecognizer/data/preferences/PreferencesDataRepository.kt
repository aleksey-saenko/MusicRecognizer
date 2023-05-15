package com.mrsep.musicrecognizer.data.preferences

import com.mrsep.musicrecognizer.UserPreferencesProto
import kotlinx.coroutines.flow.Flow

interface PreferencesDataRepository {
    val userPreferencesFlow: Flow<UserPreferencesProto>

    suspend fun saveApiToken(value: String)

    suspend fun setOnboardingCompleted(value: Boolean)

    suspend fun setNotificationServiceEnabled(value: Boolean)

    suspend fun setDynamicColorsEnabled(value: Boolean)

    suspend fun setRequiredServices(value: UserPreferencesProto.RequiredServicesProto)

    suspend fun setDeveloperModeEnabled(value: Boolean)

    suspend fun setSchedulePolicy(value: UserPreferencesProto.SchedulePolicyProto)
}