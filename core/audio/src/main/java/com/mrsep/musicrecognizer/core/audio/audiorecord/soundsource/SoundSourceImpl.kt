package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import android.util.Log
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioRecordDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.yield
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val TAG = "SoundSourceImpl"

internal class SoundSourceImpl(
    private val appContext: Context,
    private val mediaProjection: MediaProjection? = null
) : SoundSource {

    override val params = SoundSourceConfigProvider.config
    private val chunkEnergyChannel = Channel<Double>(Channel.CONFLATED)

    override val pcmChunkFlow: SharedFlow<Result<ByteArray>> = flow {
        val params = checkNotNull(params) { "No available configuration for audio recording" }
        var audioRecordRef: AudioRecord? = null
        try {
            val oneSecBuffer = params.audioFormat.sampleRate * params.bytesPerFrame
            val realBuffer = (params.minBufferSize * 10).coerceAtLeast(oneSecBuffer)
            val audioRecordBuilder = AudioRecord.Builder()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioRecordBuilder.setContext(appContext)
            }
            if (mediaProjection == null) {
                audioRecordBuilder.setAudioSource(DEFAULT_AUDIO_SOURCE)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val captureConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .addMatchingUsage(AudioAttributes.USAGE_GAME)
                        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                        .build()
                    audioRecordBuilder.setAudioPlaybackCaptureConfig(captureConfig)
                } else {
                    error("AudioPlaybackCapture API is available on Android 10+")
                }
            }
            @SuppressLint("MissingPermission")
            val audioRecord = audioRecordBuilder
                .setAudioFormat(params.audioFormat)
                .setBufferSizeInBytes(realBuffer)
                .build()
            audioRecordRef = audioRecord // False-positive lint warning, don't delete this line
            audioRecord.startRecording()
            check(audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                "AudioRecord cant start recording, is audio input captured by another app?"
            }
            while (true) {
                val pcmChunk = when (params.audioFormat.encoding) {
                    AudioFormat.ENCODING_PCM_16BIT -> {
                        ByteArray(params.chunkSize).apply {
                            audioRecord.read(
                                this,
                                0,
                                this.size,
                                AudioRecord.READ_BLOCKING
                            )
                            chunkEnergyChannel.send(this.toShortArray().calculateChunkEnergy())
                        }
                    }

                    AudioFormat.ENCODING_PCM_FLOAT -> {
                        FloatArray(params.chunkSize / Float.SIZE_BYTES).apply {
                            audioRecord.read(
                                this,
                                0,
                                this.size,
                                AudioRecord.READ_BLOCKING
                            )
                            chunkEnergyChannel.send(this.calculateChunkEnergy())
                        }.toByteArray()
                    }

                    else -> error("Unsupported encoding")
                }
                emit(Result.success(pcmChunk))
                yield()
            }
        } finally {
            audioRecordRef?.release()
        }
    }
        .catch { cause ->
            Log.e(TAG, "Error during audio recording", cause)
            emit(Result.failure(cause))
        }
        .shareIn(
            scope = CoroutineScope(AudioRecordDispatcher + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(RECORDER_SHARING_TIMEOUT_MS),
            replay = 0
        )

    override val soundLevel: StateFlow<Float> = if (params != null) {
        chunkEnergyChannel.receiveAsFlow()
            .transformEnergyToRMS(
                movingWindowSizeChunks = MOVING_WINDOW_MILLISECONDS
                    .div(params.chunkSizeInSeconds * 1000)
                    .roundToInt(),
                samplesPerChunk = params.chunkSize / params.bytesPerFrame
            )
    } else {
        emptyFlow()
    }
        .map(::transformToNormalizedDBFS)
        .map { float -> float.roundTo(2) }
        .distinctUntilChanged { old, new -> old.equalsDelta(new, 0.01f) }
        .catch { cause -> Log.e(TAG, "Error while calculating sound level", cause) }
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(0),
            initialValue = 0f
        )

    private fun ShortArray.calculateChunkEnergy() = sumOf { value -> (value / 32768.0).pow(2) }
    private fun FloatArray.calculateChunkEnergy() = sumOf { value -> value.toDouble().pow(2) }

    private fun Flow<Double>.transformEnergyToRMS(movingWindowSizeChunks: Int, samplesPerChunk: Int) = flow {
        val window = ArrayDeque<Double>(movingWindowSizeChunks)
        collect { energy: Double ->
            if (window.size == movingWindowSizeChunks) window.removeAt(0)
            window.add(energy)
            val rms = sqrt(window.sum() / (window.size * samplesPerChunk))
            emit(rms.toFloat())
        }
    }

    private fun transformToNormalizedDBFS(rmsValue: Float): Float {
        val dBFS = 20 * log10(rmsValue).coerceIn(SILENCE_THRESHOLD_DECIBELS, 0f)
        if (dBFS.isNaN()) return 0f
        return (dBFS.div(SILENCE_THRESHOLD_DECIBELS.absoluteValue) + 1).coerceIn(0f, 1f)
    }

    companion object {
        private const val DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        private const val SILENCE_THRESHOLD_DECIBELS = -60f
        private const val MOVING_WINDOW_MILLISECONDS = 400
        private const val RECORDER_SHARING_TIMEOUT_MS = 0L
    }
}

private fun FloatArray.toByteArray(): ByteArray {
    val byteBuffer = ByteBuffer
        .allocate(this.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
    byteBuffer.asFloatBuffer().put(this)
    return byteBuffer.array()
}

private fun ByteArray.toShortArray(): ShortArray {
    val output = ShortArray(size.div(Short.SIZE_BYTES))
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.nativeOrder()).asShortBuffer()
    buffer.get(output)
    return output
}

private fun Float.roundTo(decimalPlaces: Int): Float {
    check(decimalPlaces > 0) { "decimalPlaces must be > 0" }
    val factor = 10f.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor
}

private fun Float.equalsDelta(other: Float, delta: Float) = abs(this - other) < delta
