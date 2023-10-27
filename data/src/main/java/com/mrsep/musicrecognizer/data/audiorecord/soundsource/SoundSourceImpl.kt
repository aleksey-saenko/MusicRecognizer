package com.mrsep.musicrecognizer.data.audiorecord.soundsource

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.yield
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

/**
 * Some important notes about AudioRecord, that are poorly documented:
 * 1). Audio data is placed in the buffer in chunks of size getMinBufferSize() / 2.
 * This doesn't depend on the set custom buffer size or anything else.
 * 2). If reading data from the buffer is slower than filling it up, the new recorded data is lost.
 * AudioRecord keeps recording, but audio frames which exceed the buffer-level are dropped.
 * The recording will be distorted with a sense of acceleration (loss of intermediate frames).
 * 3). The first of the received samples will most likely be zero
 * due to hardware AGC (Automatic Gain Control).
 * 4). The first audio chunk arrives in the buffer with some delay (100ms in my tests).
 * Most likely it is related to a resampler. If the requested sample rate differs
 * from hardware ADC (analog-to-digital converter), then a resampler filter delay may occur.
 */

private const val TAG = "AudioSourceImpl"
private const val SHARING_TIMEOUT_MS = 0L

class SoundSourceImpl @Inject constructor() : SoundSource {

    private val audioSource = MediaRecorder.AudioSource.MIC

    override val params by lazy {
        val encodings = listOf(AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_FLOAT)
        val sampleRates = listOf(48_000, 44_100, 96_000)
        val ch = AudioFormat.CHANNEL_IN_MONO
        for (enc in encodings) {
            for (rate in sampleRates) {
                val minBufferSize = AudioRecord.getMinBufferSize(rate, ch, enc)
                if (minBufferSize > 0) {
                    val format = AudioFormat.Builder()
                        .setChannelMask(ch)
                        .setEncoding(enc)
                        .setSampleRate(rate)
                        .build()
                    return@lazy SoundSourceConfig(format, minBufferSize)
                }
            }
        }
        null
    }

    override val pcmChunkFlow: SharedFlow<Result<ByteArray>> = flow {
        val params = checkNotNull(params) { "No available params for AudioRecord" }
        val oneSecBuffer = params.audioFormat.sampleRate * params.bytesPerFrame
        val realBuffer = (params.minBufferSize * 10).coerceAtLeast(oneSecBuffer)
        var audioRecordRef: AudioRecord? = null
        try {
            @SuppressLint("MissingPermission")
            val audioRecord = AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(params.audioFormat)
                .setBufferSizeInBytes(realBuffer)
                .build()
            audioRecordRef = audioRecord
            audioRecord.startRecording()

            loopEmitWithYield {
                when (params.audioFormat.encoding) {
                    AudioFormat.ENCODING_PCM_16BIT -> {
                        ByteArray(params.chunkSize).run {
                            audioRecord.read(
                                this,
                                0,
                                this.size,
                                AudioRecord.READ_BLOCKING
                            )
                            this
                        }
                    }
                    AudioFormat.ENCODING_PCM_FLOAT -> {
                        FloatArray(params.chunkSize / Float.SIZE_BYTES).run {
                            audioRecord.read(
                                this,
                                0,
                                this.size,
                                AudioRecord.READ_BLOCKING
                            )
                            this.toByteArray()
                        }
                    }
                    else -> throw IllegalStateException("Unsupported encoding")
                }
            }
        } finally {
            audioRecordRef?.release()
        }
    }
        .catch {cause ->
            Log.e(TAG, "An error occurred during audio recording", cause)
            emit(Result.failure(cause))
        }
        .shareIn(
            scope = CoroutineScope(AudioRecordDispatcher + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MS),
            replay = 0
        )

    private suspend inline fun FlowCollector<Result<ByteArray>>.loopEmitWithYield(
        nextPcmChunk: () -> ByteArray
    ) {
        while (true) {
            val pcmChunk = nextPcmChunk()
            emit(Result.success(pcmChunk))
            yield()
        }
    }

}

private fun FloatArray.toByteArray(): ByteArray {
    val byteBuffer = ByteBuffer
        .allocate(this.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
    byteBuffer.asFloatBuffer().put(this)
    return byteBuffer.array()
}