package com.mrsep.musicrecognizer.data.audiorecord.soundsource

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
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordDispatcher
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
import kotlinx.coroutines.flow.flowOf
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

/*
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
class SoundSourceImpl(
    private val appContext: Context,
    private val mediaProjection: MediaProjection? = null
) : SoundSource {

    override val params = SoundSourceConfigProvider.config
    private val soundLevelChannel = Channel<ByteArray>(Channel.CONFLATED)

    override val pcmChunkFlow: SharedFlow<Result<ByteArray>> = flow<Result<ByteArray>> {
        val params = checkNotNull(params) { "No available params for AudioRecord" }
        val oneSecBuffer = params.audioFormat.sampleRate * params.bytesPerFrame
        val realBuffer = (params.minBufferSize * 10).coerceAtLeast(oneSecBuffer)
        var audioRecordRef: AudioRecord? = null
        try {
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
                        }.toByteArray()
                    }

                    else -> error("Unsupported encoding")
                }
                soundLevelChannel.trySend(pcmChunk)
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
        val movingWindowSizeInChunks = MOVING_WINDOW_MILLISECONDS
            .div((params.chunkSizeInSeconds * 1000))
            .roundToInt()
        when (params.audioFormat.encoding) {
            AudioFormat.ENCODING_PCM_16BIT -> soundLevelChannel.receiveAsFlow()
                .map { it.toShortArray() }
                .transformShortsToRMS(movingWindowSizeInChunks)

            AudioFormat.ENCODING_PCM_FLOAT -> soundLevelChannel.receiveAsFlow()
                .map { it.toFloatArray() }
                .transformFloatsToRMS(movingWindowSizeInChunks)

            else -> flowOf(error("Unsupported encoding"))
        }
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


    private fun transformToNormalizedDBFS(rmsValue: Float): Float {
        val dBFS = 20 * log10(rmsValue).coerceIn(SILENCE_THRESHOLD_DECIBELS, 0f)
        if (dBFS.isNaN()) return 0f
        return (dBFS.div(SILENCE_THRESHOLD_DECIBELS.absoluteValue) + 1).coerceIn(0f, 1f)
    }

    private fun Flow<ShortArray>.transformShortsToRMS(movingWindowSizeChunks: Int) = flow {
        val window = ArrayDeque<FloatArray>(movingWindowSizeChunks)
        collect { chunk: ShortArray ->
            val squares = FloatArray(chunk.size) { i -> (chunk[i] / 32768f).pow(2) }
            if (window.size == movingWindowSizeChunks) window.removeAt(0)
            window.add(squares)
            var squaresSum = 0f
            var totalSize = 0
            for (squaresArray in window) {
                squaresSum += squaresArray.sum()
                totalSize += squaresArray.size
            }
            val rms = sqrt(squaresSum / totalSize)
            emit(rms)
        }
    }

    private fun Flow<FloatArray>.transformFloatsToRMS(movingWindowSizeChunks: Int) = flow {
        val window = ArrayDeque<FloatArray>(movingWindowSizeChunks)
        collect { chunk: FloatArray ->
            val squares = FloatArray(chunk.size) { i -> chunk[i].pow(2) }
            if (window.size == movingWindowSizeChunks) window.removeAt(0)
            window.add(squares)
            var squaresSum = 0f
            var totalSize = 0
            for (squaresArray in window) {
                squaresSum += squaresArray.sum()
                totalSize += squaresArray.size
            }
            val rms = sqrt(squaresSum / totalSize)
            emit(rms)
        }
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

private fun ByteArray.toFloatArray(): FloatArray {
    val output = FloatArray(size.div(Float.SIZE_BYTES))
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.nativeOrder()).asFloatBuffer()
    buffer.get(output)
    return output
}

private fun Float.roundTo(decimalPlaces: Int): Float {
    check(decimalPlaces > 0) { "decimalPlaces must be > 0" }
    val factor = 10f.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor
}

private fun Float.equalsDelta(other: Float, delta: Float) = abs(this - other) < delta
