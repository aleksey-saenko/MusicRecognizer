package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

}