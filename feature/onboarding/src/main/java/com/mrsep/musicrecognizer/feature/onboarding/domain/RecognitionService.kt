package com.mrsep.musicrecognizer.feature.onboarding.domain

import com.mrsep.musicrecognizer.feature.onboarding.domain.model.TokenValidationStatus

interface RecognitionService {

    suspend fun validateToken(token: String): TokenValidationStatus

}