package com.mrsep.musicrecognizer.core.recognition.shazam

import android.content.Context
import android.media.AudioFormat
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.AudioDecoder
import com.mrsep.musicrecognizer.core.audio.audiorecord.decoder.AudioResampler
import com.mrsep.signature.ShazamSignature
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class ShazamSignatureGenerator @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    suspend fun generate(sample: File): Result<String> = withContext(Dispatchers.Default) {
        try {
            val decoded = AudioDecoder(appContext).decode(sample).getOrThrow()
            val resampled = AudioResampler().resample(decoded, ShazamSignature.REQUIRED_SAMPLE_RATE).getOrThrow()
            with(resampled) {
                require(
                    channelCount == 1 &&
                            sampleRate == ShazamSignature.REQUIRED_SAMPLE_RATE &&
                            pcmEncoding == AudioFormat.ENCODING_PCM_16BIT
                ) { "Failed to create shazam signature due to Illegal audio sample format" }
            }
            val signature = ShazamSignature.fromI16(resampled.data)
            Result.success(signature)
        } catch (e: Exception) {
            ensureActive()
            Result.failure(e)
        }
    }
}