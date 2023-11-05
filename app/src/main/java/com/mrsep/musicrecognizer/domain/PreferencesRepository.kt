package com.mrsep.musicrecognizer.domain

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setNotificationServiceEnabled(value: Boolean)

}