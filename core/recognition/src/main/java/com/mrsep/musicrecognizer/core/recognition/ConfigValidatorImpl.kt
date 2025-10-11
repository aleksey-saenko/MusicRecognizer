package com.mrsep.musicrecognizer.core.recognition

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.mrsep.musicrecognizer.core.domain.preferences.RecognitionServiceConfig
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSample
import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidationResult
import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidator
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionServiceFactory
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.time.Instant
import javax.inject.Inject
import kotlin.io.path.notExists
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ConfigValidatorImpl"

internal class ConfigValidatorImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val recognitionServiceFactory: RecognitionServiceFactory,
) : ConfigValidator {

    override suspend fun validate(config: RecognitionServiceConfig): ConfigValidationResult {
        val testSample = try {
            getTestSample()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read test audio sample", e)
            return ConfigValidationResult.Error.UnknownError
        }
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

    private fun getTestSample(): AudioSample {
        val cachedSample = appContext.cacheDir.resolve(TEST_SAMPLE_FILE_NAME).toPath()
        if (cachedSample.notExists()) {
            val tempFile = appContext.cacheDir.resolve("$TEST_SAMPLE_FILE_NAME.tmp").toPath()
            Files.copy(
                appContext.assets.open(TEST_SAMPLE_FILE_NAME, AssetManager.ACCESS_BUFFER),
                tempFile,
                StandardCopyOption.REPLACE_EXISTING
            )
            FileChannel.open(tempFile, StandardOpenOption.WRITE).use { ch -> ch.force(true) }
            Files.move(tempFile, cachedSample, StandardCopyOption.ATOMIC_MOVE)
        }
        return AudioSample(
            file = cachedSample.toFile(),
            duration = 3.5.seconds,
            timestamp = Instant.now(),
            mimeType = "audio/ogg"
        )
    }

    companion object {
        private const val TEST_SAMPLE_FILE_NAME = "test_sample_3500ms.ogg"
        @Suppress("unused")
        private const val AUDIO_SAMPLE_URL = "https://audd.tech/example.mp3"
    }
}
