package com.mrsep.musicrecognizer.core.recognition.shazam

import android.content.Context
import android.media.AudioFormat
import android.util.LruCache
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.AudioDecoder
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.AudioResampler
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.DecodedAudio
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import java.nio.ByteOrder

internal class ShazamSignatureGeneratorVibra @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : ShazamSignatureGenerator {

    private val cache = LruCache<String, String>(CACHE_MAX_SIZE)

    override suspend fun generate(sample: File): Result<String> = withContext(Dispatchers.Default) {
        cache[sample.absolutePath]?.let { return@withContext Result.success(it) }
        try {
            val decoded = AudioDecoder(appContext).decode(sample).getOrThrow()
            val resampled = AudioResampler().resample(decoded, VibraSignature.REQUIRED_SAMPLE_RATE).getOrThrow()
            resampled.checkFormat()
            val signature = VibraSignature.fromI16(resampled.data)
            cache.put(sample.absolutePath, signature)
            Result.success(signature)
        } catch (e: Exception) {
            ensureActive()
            Result.failure(e)
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun DecodedAudio.checkFormat() {
        val byteOrder = ByteOrder.nativeOrder()
        require(
            channelCount == 1 &&
                    sampleRate == VibraSignature.REQUIRED_SAMPLE_RATE &&
                    pcmEncoding == AudioFormat.ENCODING_PCM_16BIT &&
                    byteOrder == ByteOrder.LITTLE_ENDIAN
        ) {
            "Failed to create shazam signature due to Illegal audio sample format " +
                    "(ch = $channelCount, sampleRate = $sampleRate, enc = $pcmEncoding, order = ${byteOrder})"
        }
    }

    companion object {
        private const val CACHE_MAX_SIZE = 10
    }
}

//@Serializable
//private data class SignatureJson(
//    @SerialName("uri")
//    val uri: String,
//    @SerialName("sample_ms")
//    val sampleMs: Long,
//)