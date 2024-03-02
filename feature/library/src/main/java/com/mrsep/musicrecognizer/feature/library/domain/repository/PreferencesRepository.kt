package com.mrsep.musicrecognizer.feature.library.domain.repository

import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setTrackFilter(value: TrackFilter)
    suspend fun setUseGridForLibrary(value: Boolean)
    suspend fun setShowRecognitionDateInLibrary(value: Boolean)

}