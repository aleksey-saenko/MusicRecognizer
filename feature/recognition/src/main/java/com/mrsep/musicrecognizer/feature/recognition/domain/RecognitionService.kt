package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import java.io.File

interface RecognitionService {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        file: File
    ): RemoteRecognitionResult<Track>

}