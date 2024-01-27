package com.mrsep.musicrecognizer.data.remote

import java.net.URL
import javax.inject.Inject

internal class ConfigValidatorImpl @Inject constructor(
    private val recognitionServiceFactory: RecognitionServiceFactoryDo
): ConfigValidatorDo {

    override suspend fun validate(config: RecognitionServiceConfigDo): ConfigValidationStatusDo {
        val service = recognitionServiceFactory.getService(config)
        val testUrl = URL(AUDIO_SAMPLE_URL)
        return when (val recognitionResult = service.recognize(testUrl)) {
            is RemoteRecognitionResultDo.Error ->
                ConfigValidationStatusDo.Error(recognitionResult)
            RemoteRecognitionResultDo.NoMatches,
            is RemoteRecognitionResultDo.Success -> ConfigValidationStatusDo.Success
        }
    }

    companion object {
        private const val AUDIO_SAMPLE_URL = "https://audd.tech/example.mp3"
    }

}