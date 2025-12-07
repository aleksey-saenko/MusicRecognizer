package com.mrsep.musicrecognizer.core.recognition.shazam

import android.content.Context
import android.content.res.AssetManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.AudioDecoder
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSample
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.FileWriter
import java.security.MessageDigest
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class VibraLibrary {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                explicitNulls = false
                ignoreUnknownKeys = true
            })
        }
    }
    private val shazamService = ShazamRecognitionService(
        ioDispatcher = Dispatchers.IO,
        httpClientLazy = { httpClient },
        signatureGenerator = ShazamSignatureGeneratorVibra(context),
    )

    // Test signature generator by comparing outputs with the original signatures created by Vibra,
    // then test Shazam endpoint
    @Test
    fun rawPcmSampleTest(): Unit = runBlocking {
        val fileLogger = FileWriter(
            context.filesDir.resolve("rawPcmSampleTest.log").apply {
                delete()
                createNewFile()
            }, true
        )
        controlTestSamples.forEach { (assetName, expHash, expSignature, sampleDuration, expTitle, expArtist) ->
            fileLogger.appendLine("LocalSample(")
            fileLogger.appendLine("    url = \"${assetName}\",")
            val sampleBytes = context.assets.open(assetName, AssetManager.ACCESS_BUFFER).readBytes()
            val sampleHash = sampleBytes.sha256()
            fileLogger.appendLine("    sha256 = \"${sampleHash}\",")
            sampleHash.shouldBe(expHash, "Audio sample file is corrupted ($assetName)")
            val sampleFile = temporaryFolder.newFile().apply { writeBytes(sampleBytes) }
            // Extract raw samples. The sample must be uncompressed to avoid device-specific decoding
            val decoded = AudioDecoder(context).decode(sampleFile).getOrThrow()
            // Assume the WAV file uses a standard header without additional metadata
            val purePcm = sampleBytes.copyOfRange(44, sampleBytes.size)
            decoded.sampleRate.shouldBe(VibraSignature.REQUIRED_SAMPLE_RATE)
            check(purePcm.contentEquals(decoded.data))
            val signature = VibraSignature.fromI16(purePcm)
            fileLogger.appendLine("    vibraSignature = \"${signature}\",")
            signature.shouldBe(expSignature)
            val recognitionResult = shazamService.recognize(
                AudioSample(
                    file = sampleFile,
                    timestamp = Instant.now(),
                    duration = sampleDuration,
                    sampleRate = 16_000,
                    mimeType = "audio/x-wav"
                )
            )
            recognitionResult.shouldBeTypeOf<RemoteRecognitionResult.Success>()
            fileLogger.appendLine("    trackTitle = ${recognitionResult.track.title}")
            fileLogger.appendLine("    trackArtist = ${recognitionResult.track.artist}")
            fileLogger.appendLine("),")
            recognitionResult.track.title.shouldBe(expTitle)
            recognitionResult.track.artist.shouldBe(expArtist)
        }
        fileLogger.close()
    }

    // Device-specific regression test including decoding and resampling steps
    @Test
    fun combinedRegressionTest(): Unit = runBlocking {
        val fileLogger = FileWriter(
            context.filesDir.resolve("combinedRegressionTest.log").apply {
                delete()
                createNewFile()
            }, true
        )
        regressionTestSamples.forEach { (sampleUrl, expectedHash, expectedSignature) ->
            fileLogger.appendLine("RemoteSample(")
            fileLogger.appendLine("    url = \"${sampleUrl}\",")
            val sampleBytes = httpClient.get(sampleUrl).bodyAsBytes()
            val sampleHash = sampleBytes.sha256()
            fileLogger.appendLine("    sha256 = \"${sampleHash}\",")
            sampleHash.shouldBe(expectedHash, "Remote audio sample file has been changed ($sampleUrl)")
            val sampleFile = temporaryFolder.newFile().apply { writeBytes(sampleBytes) }
            val signature = ShazamSignatureGeneratorVibra(context).generate(sampleFile).getOrThrow()
            fileLogger.appendLine("    vibraSignature = \"${signature}\",")
            fileLogger.appendLine("),")
            signature.shouldBe(expectedSignature)
        }
        fileLogger.close()
    }

    companion object {

        // Audio must be mono, 16-bit PCM LE, 16 kHz, WAV to skip device-specific decoding/resampling
        @Suppress("SpellCheckingInspection")
        private val controlTestSamples = listOf(
            LocalSample(
                filename = "ff-s16le-1c-16khz-46s51s.wav",
                sha256 = "c2136288024363466b1a9dc1267eb293be35fe9f9674bc78ceabd6acb05a6ed1",
                vibraSignature = "data:audio/vnd.shazam.sig;base64,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",
                duration = 5.seconds,
                trackTitle = "Furious Freak",
                trackArtist = "Kevin MacLeod",
            ),
        )

        // MUST BE MONO. The regression samples may be in any sample rate/device-supported format
        // See: https://docs.espressif.com/projects/esp-adf/en/latest/design-guide/audio-samples.html
        @Suppress("SpellCheckingInspection")
        private val regressionTestSamples = listOf(
            RemoteSample(
                url = "https://audd.tech/example.mp3",
                sha256 = "86544a7dfb3b8486ae648fbcf2a63a843d423d5f602e25b24fbf121f5b86f2b6",
                vibraSignature = "data:audio/vnd.shazam.sig;base64,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",
            ),
            RemoteSample(
                url = "https://dl.espressif.com/dl/audio/gs-16b-1c-44100hz.aac",
                sha256 = "3e692c130e3022927b3a8f999303cc48a0fd40ddd058caa012c4b8773221ea4c",
                vibraSignature = "data:audio/vnd.shazam.sig;base64,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",
            ),
            RemoteSample(
                url = "https://dl.espressif.com/dl/audio/gs-16b-1c-44100hz.m4a",
                sha256 = "4f83af0ab99460fea337838beeeb08b7616651701e894570e0c087004b2fd726",
                vibraSignature = "data:audio/vnd.shazam.sig;base64,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",
            ),
            RemoteSample(
                url = "https://dl.espressif.com/dl/audio/gs-16b-1c-44100hz.wav",
                sha256 = "0f22e08bc78d7d8051c73529e9d7b0443dd3a776627efd4c6fccf9e79424807d",
                vibraSignature = "data:audio/vnd.shazam.sig;base64,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",
            ),
        )

        @ClassRule
        @JvmField
        val temporaryFolder = TemporaryFolder()
    }
}

private data class RemoteSample(
    val url: String,
    val sha256: String,
    val vibraSignature: String,
)

private data class LocalSample(
    val filename: String,
    val sha256: String,
    val vibraSignature: String,
    val duration: Duration,
    val trackTitle: String,
    val trackArtist: String,
)

private fun ByteArray.sha256(): String = MessageDigest.getInstance("SHA-256")
    .digest(this)
    .joinToString("") { "%02x".format(it) }