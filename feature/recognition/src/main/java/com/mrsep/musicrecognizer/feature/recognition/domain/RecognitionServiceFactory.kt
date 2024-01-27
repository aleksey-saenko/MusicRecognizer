package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionServiceConfig

interface RecognitionServiceFactory {

    fun getService(config: RecognitionServiceConfig): RemoteRecognitionService

}