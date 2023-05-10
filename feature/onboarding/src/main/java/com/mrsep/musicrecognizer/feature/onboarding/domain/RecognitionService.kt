package com.mrsep.musicrecognizer.feature.onboarding.domain

import com.mrsep.musicrecognizer.feature.onboarding.domain.model.RemoteRecognitionResult

interface RecognitionService {

    suspend fun validateToken(token: String): RemoteRecognitionResult<Unit>

}