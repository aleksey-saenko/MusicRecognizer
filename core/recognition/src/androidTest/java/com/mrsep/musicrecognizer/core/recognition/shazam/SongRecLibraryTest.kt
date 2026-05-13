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
class SongRecLibraryTest {

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
        signatureGenerator = ShazamSignatureGeneratorSongRec(context),
    )

    // Test signature generator by comparing outputs with the original signatures created by SongRec,
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
            decoded.sampleRate.shouldBe(SongRecSignature.REQUIRED_SAMPLE_RATE)
            check(purePcm.contentEquals(decoded.data))
            val signature = ShazamSignatureGeneratorSongRec(context)
                .generate(sampleFile)
                .getOrThrow()
            fileLogger.appendLine("    signature = \"${signature}\",")
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
            val signature = ShazamSignatureGeneratorSongRec(context)
                .generate(sampleFile)
                .getOrThrow()
            fileLogger.appendLine("    signature = \"${signature}\",")
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
                filename = "ss-s16le-1c-16khz.wav",
                sha256 = "4d106f577e6331b619e31d880fc8ce2479437d92c3033a9a66ffa3afcccc4793",
                signature = "data:audio/vnd.shazam.sig;base64,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",
                duration = 5.seconds,
                trackTitle = "Sneaky Snitch",
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
                signature = "data:audio/vnd.shazam.sig;base64,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",
            ),
            RemoteSample(
                url = "https://dl.espressif.com/dl/audio/gs-16b-1c-44100hz.aac",
                sha256 = "3e692c130e3022927b3a8f999303cc48a0fd40ddd058caa012c4b8773221ea4c",
                signature = "data:audio/vnd.shazam.sig;base64,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",
            ),
            RemoteSample(
                url = "https://dl.espressif.com/dl/audio/gs-16b-1c-44100hz.m4a",
                sha256 = "4f83af0ab99460fea337838beeeb08b7616651701e894570e0c087004b2fd726",
                signature = "data:audio/vnd.shazam.sig;base64,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",
            ),
            RemoteSample(
                url = "https://dl.espressif.com/dl/audio/gs-16b-1c-44100hz.wav",
                sha256 = "0f22e08bc78d7d8051c73529e9d7b0443dd3a776627efd4c6fccf9e79424807d",
                signature = "data:audio/vnd.shazam.sig;base64,gCX+yid1ODZkDAAAAJwRlAAAAAAAAAAAAAAAAAAAABgAAAAAAAAAAAD9AgAAAHwAAAAAQGQMAABAAANgggAAABzqXcYPAoRphgwzn3BGDh4XZp4PJkxtRw5NhmjSDyKyc5kKSINnNQ04dG+nCmThbMgKDq5fwQ8zUW4ECwAsZJUOSdBkzg0t+FvQDwEfbZcKFppexA0oE1fTDyHKadMKFKpbwQ9VIHCYCgjOYdsPK0JnMg0LkmuLCv+DBQAAALRPmwoAAEEAA2DWAQAADKldoSUBTF6BHxFqZ7QXA35Z5isgrXAtFQDGWgoZAMhoWCoOO2sQJAGocDgTAD5dwy0EymbUFwSEXi8cF4xdux4JhWiWJR5jcf0SAl1qthcYzWmSJQFFXDkcFb5fDRQUal+0LQppbq8fByRuuxcT1V+iIgM1X4MdAphhmycDdmvOGA85ZjgqBbp1ExUi/m5CGQI3WysRBotmKyoPS1uIHwO4YKclKDdo6RwCYl7rFwFRYqoSApRa7CIDN2Z5JxYyelgqAfxclB8AxWPcJA1edhcVOxhnBysDlniuIAW/aEcVE/pd3yQoznQbFQgIa1UqHPVcuxAANleNHQH2U1koAaVb3yQkwnQdFQEqa1UqHj1fdBEAmV3KFwDOXbsjAfFv+yoBblvuGwNmeH8gCQtdTR0iFVnXJDdrV+EeAhli3SQCgGR7JwE+dSMVAj1pVipA/FtNGhhNcxMVDPFkuBILVWGwHw+RfDIqNA1bmx9D3VGaHwv4TqQXEwpHqBMe93ooKgbgTQsVOGhGih8N9UeOFw45RUUbAEp6JSoTd0GlExoTOwEZEmQ9NRoXMT+IGxj6PkkcAKd7MyoBiT4oGQCzQzAijEM3yR8BGzmAFwI1PgYUIdk1kiUAAEIAA2DxBAAACddYxF0BDFpFMgC8WhVHAKlYLmoBD1sASgdOVkRPAVddrTQBDlV7aQEpWv5eArxdgzgBiVpTLwkWTohkBR9Xq1MHD1I+ZQJ8UbVNBTFd0j8VcF3XQQEsYPdUAVVWu2oBvle9WQB5WBpdA/9W0koDemDkNAKLaAhjEj9kPz8JBlqlQwDuWCpZAKVWhGQA91vSbAHEWD9NAI9W2WAAhlbIZgFYYJI4ANVZzUkF914ALwcFYrgzK61aLT0ASVbAQQD5YKdeAGtRMGQAylr6bAO2VAFVBpFnCEcDl1FESgClTXRPAqxPcFcOiVNOUQWaVQNoCl5XRkIC7lJHTAJfVIhbAbRd4zQCh14HYwKeWF0/AVFVEFICC1IpPAQoVT5OBJlgl14Bw2dZLw4wVRtsAUBShlMBqVk4SQA7VcVvBVBcCEUC+k9lTwGeUwBYAZ5bKToCk18QSgM3XY8xB5dZTz8LaWlTSwAUT9xqFDtYCmQUWGViMg2HTzRdBPtXX28BzV7tRAFQW3tlABVY42oHN1W7UQRnWMFiAjdNj1cCb09KXhpYUBtaAYdTsEgALlR3TwGpWFU/AdJZ1FMAiFWHbAFlW5BFAOddAGQBWVgxMQEhe6Q4CSBTMWoQsWG8VAG0U2gvFqNKOG0CXk2YNBHMR2leHzhLWVwBMGPwNQClVLM8APlYW0sABk7FUgHvU/VrAQRV8FYBkVJALwCDUOhEAsBj9mEB22Y2QR30SVBIAGNKg2gd+1cvOAUyTo9qAlpijT8B7FDqSR5SU4JRAdhUsDYA+VVVRAC4VaNLALRW7lYAg1KFXQCxTdtoAMtRmWsB/FQ+RwABVk9UAF1UNmIB2FCXOwEvV7kwDr5N2GQV9WJ5Px5WWZteAJtc7WgBXFonWQHMXvQ4ALde1EkAXV/mTwAGX/xrAW1elTQDN2caQQGQYHphPLBO7WgBSEp8QgA9TPhZAMNK3FwABVESZgFDSvVFIoJQdDMAz012bAHGVqM2AENT7zkAx1H5VgLTVhxKAUlQfS8AwEt8RAEIVTpPAC1Jg18ErmKCPxknTABbAjJMxEwBT0xARRAlSPttAcpFoUYGKk7IZgHUUmhfAiRbK2oHlEtpTgTXUnpVG1JZHEoCxVj3WwHDVmlDAONSwU0Ak1k8ZAKQWzk3AOlhAjoArVbQbAHxVoBoAfFhTj8GplN5XwU6XR4zAwRmUlQISk1pTw3/XYdTJHA+jm0Gg0jsMQ9vYWZUA9g+VFwXBjBAaROTPMpgAgY8VF0VnmRFVAFMQ05lAXg9aF4AyD9CbQHHThE7AQVF/jISiEk7QwDTPkxMAxQzAVgSd0g1MgOdNSpPAcQxOlkA/js5YAGsOttcE7BiTlQBrjJqRgItPLxfEsE8W0wAeEJpZQHqSENDAO06R1wB5j5NbQJ9RgszEsIrgWgDVTiRYgIXSAwyEhM2SmAEYVEEOwEgOgU2EVg30mABQD0HNxRXLHppAeRAdmUBmTxYTAGpRSszExk4Qk8D1TfsSAB1On1dFik9VFwC7z1VXwfmRH9CC7Bid1QB70F5ZQD3QJNtAcRKQkMAxD0wSwEEQC43ACJONjsBSzBraAFrQwQyiMUeuUcBlSyoPgAfH6RGABQecEsATiNEWwASGkBeABcYgGQAfyATayNaNkA2ABIpDVIBhhzSSgBhIMVZABcYgGEAAABDAANg7AQAAAmIVJ56AClP+IEAi0p8oACPTZKsAWFUj4UAxU/ulgC5R5mpAYNXbnYDHlZ8fQHSUXukAu1JDZgHhUtGiRXYUo+NAE1NeZEAiE0slgDbUD+bAOBOLacBNVE+cgKDSwKBCr1LwacD6EkLlQBLS02vBKJakKILSk6eqQR5T8CCAEdZiYoeT1WNlgERWANxAFpX8ngASlgDfgCBVsaEAAJPKqgAOFDirACQUN+vAVpSG5AAiVQXmgLhUiqNA9VXpnUHiVB5pSlOTqmcAKxMj6wBtlQ3hQGsUaaiAl1KAJIEtlLAcQHaW2x2BS5NG3oB3UgBkwCQTMauASNX+3IAg0zHrQHZUVN9EHhWBI4EM1RPhgAcSn+RAJhV16kKCVQ7igB+VJSlAVRPz3wAvE6/lgKHT8yCBbNTdXMLlmFJdg+7VK6VARVVQIQAEFO3kADBTnWbAN9MGqsByVTOfwGBSFqgABNNx6gD71JxpAOyVO2KAVpbj3sF4FoEfBgOWxJ9IP1eTZgLF2DvewBaU3KtBhJP/nIAzVLykAD1T4CqBNZarX4Bw1qtdgCSUT2EAF9f+pcCsVG7hwOuT1igANVKNqQGHFnFfwAGVHuFAPlMjqMfyVTLiwH+Vg91ALRSyYQABVCmkgDpT5+bAJBQQJ4COlPjfAHNTFOlAQJZ3KkCPHUycQP/WPR/DTdYl38Iu1jufhWlRQCVAZ9EuZoARUbjnwANSw+nAHRJ760SC0m1igE3R4mvIGJNtnQAi1H4kwDqSpScAIZPM6UAnEswqwFpTuZ3ANhOaXwAGE+XfwBBVhaDAK9LeYgA206bkCEBS4CZATVNTn0Am0gLgSDQTJOOAdBLrosBE0/dhiG1TyukAIhMd6cADE1WrwFqTZh3AGZOp4EASE/aiAD+UDOQAOVQuZkBe1FOcQBMUml9EHhO0XQAskb8fjPjWux5APBWAH4AAlrahgBJXH+NALdT0KYB4Fg1dgD1WC2BAMFdiZYApFU9mgAjWHOiAKJZbqoCZlxTcTBbQj+gEeNIuHEAWlAqdQD7RsyBAMhMQIcjYlOEeADeUA6QAP1R/ZMAjVLvmAC8UDueAK5MjKEBqFL3igDESkmlAApNi6wDtFGMhSCeScpxHkhPjpUCQE5YkQFIUoGNAMFJVp4CA0vyhx4aUtmEAZtTjYwA7FCKqwFnU4V6AO9NKKEB209nkAC7VJSbAA1OMqQBAVFUkwV3Wv5wBqpZc34CwEgRrws6VMaoDDFMTqcpRi+SnRGbMuSDAUNO16gAyj1ErgIbMbOfC41CjaYLi1mIfgHXLFOTEnwzwoUB5DDqmwFfNsCXAto9iI8CVjGsoALiMpqGEKhRwagXbjN6dhX5MUqEALk0t4cA6it2iwCbOL+XAdkvh50BJzFOcwDxO5CPAzQ0PIcRxjPAoAItN4+GAf4pv5oWljNAdgGtM4aEElctAXQBqVeUfgARN8CXAPZUvqgCqDiOjxSRLL2TBOMweJ8IOUlzpgt8MbKgAUYve4sDLjQylxGCV8B+AL07u48CmTSMdgALP0OuAQg6hoYCVi6KnRO9L8GJAQcpxJIHSCb3mg7GNRKEFtw0lXYAmTwwrgF6LMWTACIvOJ0B1Dd1hgFjMnBzAkIyNpeHtxlPjAFqG/VzAewYMokApx3ukiLIHNp1ALkmJ5UANxpSpQ==",
            ),
        )

        @ClassRule
        @JvmField
        val temporaryFolder = TemporaryFolder()
    }
}

//internal data class RemoteSample(
//    val url: String,
//    val sha256: String,
//    val signature: String,
//)
//
//internal data class LocalSample(
//    val filename: String,
//    val sha256: String,
//    val signature: String,
//    val duration: Duration,
//    val trackTitle: String,
//    val trackArtist: String,
//)
//
//internal fun ByteArray.sha256(): String = MessageDigest.getInstance("SHA-256")
//    .digest(this)
//    .joinToString("") { "%02x".format(it) }