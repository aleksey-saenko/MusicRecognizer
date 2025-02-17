package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.preferences.RecognitionServiceConfig

interface RecognitionServiceFactory {

    fun getService(config: RecognitionServiceConfig): RemoteRecognitionService
}
