package com.mrsep.musicrecognizer.core.audio.audiorecord.decoder

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

@OptIn(UnstableApi::class)
class AudioResampler {

    suspend fun resample(
        decodedAudio: DecodedAudio,
        outputSampleRate: Int
    ): Result<DecodedAudio> = withContext(Dispatchers.Default) {
        if (decodedAudio.sampleRate == outputSampleRate) return@withContext Result.success(decodedAudio)
        var sonicRef: AudioProcessor? = null
        try {
            val sonic: AudioProcessor = SonicAudioProcessor().apply {
                setOutputSampleRateHz(outputSampleRate)
            }
            sonicRef = sonic
            val inputFormat = AudioProcessor.AudioFormat(
                /* sampleRate = */ decodedAudio.sampleRate,
                /* channelCount = */ decodedAudio.channelCount,
                /* encoding = */ decodedAudio.pcmEncoding
            )
            val outputFormat = sonic.configure(inputFormat)
            // Prepare Sonic
            sonic.flush()

            val inputBuf = ByteBuffer.wrap(decodedAudio.data).order(ByteOrder.nativeOrder())
            sonic.queueInput(inputBuf)
            sonic.queueEndOfStream()

            val outputChunks = mutableListOf<ByteArray>()
            var outputChunksByteSize = 0

            while (!sonic.isEnded) {
                ensureActive()
                val outputBuffer = sonic.output
                if (!outputBuffer.hasRemaining()) continue
                val chunk = ByteArray(outputBuffer.remaining())
                outputBuffer.get(chunk)
                outputChunks.add(chunk)
                outputChunksByteSize += chunk.size
            }
            sonic.reset()

            val resampledData = if (outputChunks.size == 1) {
                outputChunks[0]
            } else {
                ByteArray(outputChunksByteSize).also {
                    var dest = 0
                    for (chunk in outputChunks) {
                        System.arraycopy(chunk, 0, it, dest, chunk.size)
                        dest += chunk.size
                    }
                }
            }
            Result.success(DecodedAudio(
                data = resampledData,
                channelCount = outputFormat.channelCount,
                sampleRate = outputFormat.sampleRate,
                pcmEncoding = outputFormat.encoding,
            ))
        } catch (e: Exception) {
            ensureActive()
            Result.failure(e)
        } finally {
            sonicRef?.reset()
        }
    }
}
