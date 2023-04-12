package com.mrsep.musicrecognizer.feature.onboarding.domain

import com.mrsep.musicrecognizer.feature.onboarding.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.Track
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.UserPreferences
import java.net.URL

interface RecognitionService {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        url: URL
    ): RemoteRecognitionResult<Track>

}