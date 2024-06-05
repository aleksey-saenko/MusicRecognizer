package com.mrsep.musicrecognizer.feature.track.domain

import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setLyricsFontStyle(value: UserPreferences.LyricsFontStyle)
}
