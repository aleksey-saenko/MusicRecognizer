package com.mrsep.musicrecognizer.data.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesRepositoryDo {

    val userPreferencesFlow: Flow<UserPreferencesDo>

    suspend fun saveApiToken(value: String)

    suspend fun setOnboardingCompleted(value: Boolean)

    suspend fun setNotificationServiceEnabled(value: Boolean)

    suspend fun setDynamicColorsEnabled(value: Boolean)

    suspend fun setRequiredServices(value: UserPreferencesDo.RequiredServicesDo)

    suspend fun setDeveloperModeEnabled(value: Boolean)

    suspend fun setSchedulePolicy(value: UserPreferencesDo.SchedulePolicyDo)

    suspend fun setLyricsFontStyle(value: UserPreferencesDo.LyricsFontStyleDo)

}