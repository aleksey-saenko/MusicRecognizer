package com.mrsep.musicrecognizer.feature.library.domain.model

data class UserPreferences(
    val trackFilter: TrackFilter,
    val useGridForLibrary: Boolean,
    val showRecognitionDateInLibrary: Boolean
)