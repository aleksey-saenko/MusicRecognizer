package com.mrsep.musicrecognizer.core.recognition.shazam

import android.content.Context
import android.media.AudioFormat
import android.util.LruCache
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.AudioDecoder
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.AudioResampler
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.DecodedAudio
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.toShortArray
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import java.nio.ByteOrder
import kotlin.time.Duration.Companion.seconds

internal class ShazamSignatureGeneratorSongRec @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : ShazamSignatureGenerator {

    private val cache = LruCache<String, String>(CACHE_MAX_SIZE)

    override suspend fun generate(sample: File): Result<String> = withContext(Dispatchers.Default) {
        cache[sample.absolutePath]?.let { return@withContext Result.success(it) }
        try {
            val decoded = AudioDecoder(appContext).decode(sample).getOrThrow()
            val resampled = AudioResampler().resample(decoded, SongRecSignature.REQUIRED_SAMPLE_RATE).getOrThrow()
            resampled.validate()
            val buffer = resampled.extractCentered12Seconds()
            val signature = SongRecSignature.fromPcm16Mono16kHz(buffer)
            cache.put(sample.absolutePath, signature)
            Result.success(signature)
        } catch (e: Exception) {
            ensureActive()
            Result.failure(e)
        }
    }

    // Extracts exactly a 12-second sample from PCM audio (16 kHz, 16-bit)
    // Replicates the logic of SongRec SignatureGenerator::make_signature_from_file
    fun DecodedAudio.extractCentered12Seconds(): ShortArray {
        val targetSamples = 12 * 16_000
        val bytesPerSample = 2
        val totalSamples = data.size / bytesPerSample

        if (totalSamples == targetSamples) return data.toShortArray()

        val result = ShortArray(targetSamples)

        // If shorter -> pad with zeros at the end
        if (totalSamples < targetSamples) {
            ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(result, 0, totalSamples)
            return result
        }

        // If longer -> take 12 seconds from the middle
        val startSample = (totalSamples - targetSamples) / 2
        val startByte = startSample * bytesPerSample
        val lengthInBytes = targetSamples * bytesPerSample
        ByteBuffer.wrap(data, startByte, lengthInBytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .get(result)
        return result
    }

    @Throws(IllegalArgumentException::class)
    private fun DecodedAudio.validate() {
        val byteOrder = ByteOrder.nativeOrder()
        require(
            channelCount == 1 &&
                    sampleRate == SongRecSignature.REQUIRED_SAMPLE_RATE &&
                    pcmEncoding == AudioFormat.ENCODING_PCM_16BIT &&
                    byteOrder == ByteOrder.LITTLE_ENDIAN &&
                    data.isNotEmpty() && data.size % 2 == 0
        ) {
            "Failed to create Shazam signature due to Illegal audio sample format " +
                    "(ch = $channelCount, sampleRate = $sampleRate, enc = $pcmEncoding, " +
                    "order = ${byteOrder}, dataSize = ${data.size})"
        }
        val resampledDuration = (data.size / SongRecSignature.REQUIRED_SAMPLE_RATE).seconds
        require(resampledDuration >= FILE_MIN_DURATION) {
            "Failed to create Shazam signature; the audio sample is too short ($resampledDuration)"
        }
    }

    companion object {
        private const val CACHE_MAX_SIZE = 10
        private val FILE_MIN_DURATION = 2.seconds
    }
}
