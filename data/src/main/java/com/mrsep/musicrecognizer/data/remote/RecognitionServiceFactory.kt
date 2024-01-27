package com.mrsep.musicrecognizer.data.remote

import com.mrsep.musicrecognizer.data.remote.acrcloud.AcrCloudRecognitionService
import com.mrsep.musicrecognizer.data.remote.audd.AuddRecognitionService
import javax.inject.Inject

interface RecognitionServiceFactoryDo {

    fun getService(config: RecognitionServiceConfigDo): RecognitionServiceDo

}

internal class RecognitionServiceFactoryImpl @Inject constructor(
    private val acrCloudRecognitionServiceFactory: AcrCloudRecognitionService.Factory,
    private val auddRecognitionServiceFactory: AuddRecognitionService.Factory
): RecognitionServiceFactoryDo {

    override fun getService(config: RecognitionServiceConfigDo): RecognitionServiceDo {
        return when (config) {
            is AcrCloudConfigDo -> acrCloudRecognitionServiceFactory.create(config)
            is AuddConfigDo -> auddRecognitionServiceFactory.create(config)
        }
    }

}