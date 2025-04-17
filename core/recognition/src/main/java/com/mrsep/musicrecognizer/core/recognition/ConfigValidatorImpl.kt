package com.mrsep.musicrecognizer.core.recognition

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.mrsep.musicrecognizer.core.domain.preferences.RecognitionServiceConfig
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidationResult
import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidator
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionServiceFactory
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ConfigValidatorImpl"

internal class ConfigValidatorImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val recognitionServiceFactory: RecognitionServiceFactory,
) : ConfigValidator {

    override suspend fun validate(config: RecognitionServiceConfig): ConfigValidationResult {
        val sampleData = try {
             appContext.assets
                 .open("test_sample_3500ms.ogg", AssetManager.ACCESS_BUFFER)
                 .use { it.readBytes() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read test audio sample", e)
            return ConfigValidationResult.Error.UnknownError
        }
        val testSample = AudioRecording(
            data = sampleData,
            duration = 3.5.seconds,
            nonSilenceDuration = 3.5.seconds,
            startTimestamp = Instant.now(),
            isFallback = false
        )
        val service = recognitionServiceFactory.getService(config)
        return when (service.recognize(testSample)) {
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
        @Suppress("unused")
        private const val AUDIO_SAMPLE_URL = "https://audd.tech/example.mp3"
    }
}
