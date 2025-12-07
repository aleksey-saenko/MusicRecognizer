package com.mrsep.musicrecognizer.core.recognition.shazam

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSample
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeTypeOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class ShazamServiceTest {

    // Test undocumented Shazam endpoint with original signature
    @Test
    @Suppress("SpellCheckingInspection")
    fun endpointTest(): Unit = runBlocking {
        // Signature created by original Shazam app version 16.19.1-251119
        val goldSignature = "data:audio/vnd.shazam.sig;base64,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"
        val shazamService = ShazamRecognitionService(
            ioDispatcher = Dispatchers.IO,
            httpClientLazy = {
                HttpClient(OkHttp) {
                    install(ContentNegotiation) {
                        json(Json {
                            explicitNulls = false
                            ignoreUnknownKeys = true
                        })
                    }
                }
            },
            signatureGenerator = object : ShazamSignatureGenerator {
                override suspend fun generate(sample: File) = Result.success(goldSignature)
            },
        )
        val result = shazamService.recognize(
            AudioSample(
                file = File("non-existing"),
                timestamp = Instant.now(),
                duration = 10.seconds,
                sampleRate = 16_000,
                mimeType = "audio/*"
            )
        )
        result.shouldBeTypeOf<RemoteRecognitionResult.Success>()
        result.track.title.shouldContain("Dadada")
    }
}
