package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.domain.preferences.AcrCloudConfig
import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.preferences.RecognitionServiceConfig
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionServiceFactory
import com.mrsep.musicrecognizer.core.domain.recognition.RemoteRecognitionService
import com.mrsep.musicrecognizer.core.recognition.acrcloud.AcrCloudRecognitionService
import com.mrsep.musicrecognizer.core.recognition.audd.AuddRecognitionService
import javax.inject.Inject

internal class RecognitionServiceFactoryImpl @Inject constructor(
    private val acrCloudRecognitionServiceFactory: AcrCloudRecognitionService.Factory,
    private val auddRecognitionServiceFactory: AuddRecognitionService.Factory
) : RecognitionServiceFactory {

    override fun getService(config: RecognitionServiceConfig): RemoteRecognitionService {
        return when (config) {
            is AcrCloudConfig -> acrCloudRecognitionServiceFactory.create(config)
            is AuddConfig -> auddRecognitionServiceFactory.create(config)
        }
    }
}
