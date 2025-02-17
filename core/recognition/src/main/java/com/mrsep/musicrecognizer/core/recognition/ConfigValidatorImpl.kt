package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.domain.preferences.AcrCloudConfig
import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.preferences.RecognitionServiceConfig
import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidationResult
import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidator
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.acrcloud.AcrCloudRecognitionService
import com.mrsep.musicrecognizer.core.recognition.audd.AuddRecognitionService
import java.net.URL
import javax.inject.Inject

internal class ConfigValidatorImpl @Inject constructor(
    private val acrCloudRecognitionServiceFactory: AcrCloudRecognitionService.Factory,
    private val auddRecognitionServiceFactory: AuddRecognitionService.Factory,
) : ConfigValidator {

    override suspend fun validate(config: RecognitionServiceConfig): ConfigValidationResult {
        val testUrl = URL(AUDIO_SAMPLE_URL)
        val recognitionResult = when (config) {
            is AuddConfig -> auddRecognitionServiceFactory.create(config).recognize(testUrl)
            is AcrCloudConfig -> acrCloudRecognitionServiceFactory.create(config).recognize(testUrl)
        }
        return when (recognitionResult) {
            is RemoteRecognitionResult.Success,
            RemoteRecognitionResult.NoMatches -> ConfigValidationResult.Success

            RemoteRecognitionResult.Error.AuthError -> ConfigValidationResult.Error.AuthError
            RemoteRecognitionResult.Error.ApiUsageLimited -> ConfigValidationResult.Error.ApiUsageLimited
            RemoteRecognitionResult.Error.BadConnection -> ConfigValidationResult.Error.BadConnection
            is RemoteRecognitionResult.Error.BadRecording,
            is RemoteRecognitionResult.Error.HttpError,
            is RemoteRecognitionResult.Error.UnhandledError -> ConfigValidationResult.Error.UnknownError
        }
    }

    companion object {
        private const val AUDIO_SAMPLE_URL = "https://audd.tech/example.mp3"
    }
}
