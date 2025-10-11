package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity.AUDIO_SERVICE
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "VisualizerSoundSource"

internal class VisualizerSoundSource(
    appContext: Context,
): SoundSource {

    override val audioSource = AudioSource.DEVICE

    private val audioManager = appContext.getSystemService(AUDIO_SERVICE) as AudioManager
    private val deviceDefaultOutputSampleRate = audioManager
        .getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toInt()

    override val params = deviceDefaultOutputSampleRate?.let {
        SoundSourceConfig(
            audioFormat = AudioFormat.Builder()
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(deviceDefaultOutputSampleRate)
                .build(),
            minBufferSize = 1024 * 2 * 2 // 1024 samples
        )
    }

    private val soundLevelMeter = SoundLevelMeter(params)
    override val soundLevel by soundLevelMeter::soundLevel

    override val pcmChunkFlow: SharedFlow<Result<ByteArray>> = flow {
        val params = checkNotNull(params) {
            "$TAG: No available configuration for audio recording"
        }
        if (params.audioFormat.sampleRate != 48_000) {
            Log.w(TAG, "Untested device output sample rate = ${params.audioFormat.sampleRate}")
        }
        var visualizerRef: Visualizer? = null
        try {
            val visualizer = Visualizer(0).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                scalingMode = Visualizer.SCALING_MODE_NORMALIZED
                measurementMode = Visualizer.MEASUREMENT_MODE_NONE
                enabled = true
            }
            visualizerRef = visualizer // False-positive lint warning, don't delete this line

            var lastCaptureTime = System.nanoTime()
            while (true) {
                val bytes = ByteArray(visualizer.captureSize)
                // Don't use callback as its capture rate is fixed at 50ms
                visualizer.getWaveForm(bytes)
                val currentCaptureTime = System.nanoTime()
                val expectedDuration = currentCaptureTime - lastCaptureTime
                lastCaptureTime = currentCaptureTime
                emit(WaveCapture(bytes, expectedDuration, params.audioFormat.sampleRate))
                // If we capture too fast we get the same data (ineffective)
                // If we capture slower than 1024 samples (~20ms) we lose data
                // Selected delay 3ms empirically, but it can be adjusted later
                delay(3)
            }
        } finally {
            visualizerRef?.enabled = false
            visualizerRef?.release()
        }
    }
        .buffer(Channel.UNLIMITED)
        .clearOverlap()
        .map(::convert8BitUnsignedTo16BitSigned)
        .chunked(params?.chunkSize ?: 0)
        .onEach { soundLevelMeter.processNewChunk(it.toShortArray()) }
        .map { Result.success(it) }
        .catch { cause ->
            Log.e(TAG, "Error during audio recording", cause)
            emit(Result.failure(cause))
        }
        .shareIn(
            scope = CoroutineScope(VisualizerRecordDispatcher + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(0),
            replay = 0
        )
}

private class WaveCapture(
    val snapshot: ByteArray,
    private val expectedNewDurationNanos: Long,
    private val sampleRate: Int,
) {
    val expectedNewSamples get() = ((expectedNewDurationNanos / 1_000_000_000.0) * sampleRate).toInt()
}

fun Flow<ByteArray>.chunked(chunkSize: Int): Flow<ByteArray> = flow {
    val buffer = ByteArray(chunkSize)
    var bufferSize = 0
    collect { bytes ->
        var offset = 0
        while (offset < bytes.size) {
            // Free space in buffer
            val available = chunkSize - bufferSize
            val bytesToCopy = minOf(available, bytes.size - offset)
            System.arraycopy(
                bytes, offset,
                buffer, bufferSize,
                bytesToCopy
            )
            bufferSize += bytesToCopy
            offset += bytesToCopy
            if (bufferSize == chunkSize) {
                emit(buffer.copyOf())
                bufferSize = 0
            }
        }
    }
}

private fun Flow<WaveCapture>.clearOverlap(): Flow<ByteArray> = flow {

    fun findOverlap(previous: ByteArray, current: ByteArray): Int {
        val maxOverlap = minOf(previous.size, current.size)
        for (overlap in maxOverlap downTo 0) {
            var match = true
            for (i in 0 until overlap) {
                if (previous[previous.size - overlap + i] != current[i]) {
                    match = false
                    break
                }
            }
            if (match) return overlap
        }
        return 0
    }

    var previousChunk: ByteArray? = null
    collect { capture ->
        val newChunk = capture.snapshot
        previousChunk?.let { prevChunk ->
            val overlap = findOverlap(prevChunk, newChunk)
            val newBytes = newChunk.copyOfRange(overlap, newChunk.size)
            if (newBytes.isNotEmpty()) {
                emit(newBytes)
            } else {
                if (isSilencedChunk(newChunk)) {
                    val silencedChunk = silencedChunkUnsigned8Bit(capture.expectedNewSamples)
                    emit(silencedChunk)
                } else {
                    // The probability of this event should be extremely low
                    // Increase wave capture rate otherwise
                    Log.w(TAG, "New chunk repeats previous one " +
                            "(expectedNewSamples=${capture.expectedNewSamples})")
                }
            }
        } ?: run {
            emit(newChunk)
        }
        previousChunk = newChunk
    }
}

private fun isSilencedChunk(unsigned8BitSamples: ByteArray): Boolean {
    return unsigned8BitSamples.all { it.toInt() == -128 }
}

private fun silencedChunkUnsigned8Bit(samples: Int): ByteArray {
    return ByteArray(samples) { -128 }
}

private fun convert8BitUnsignedTo16BitSigned(input: ByteArray): ByteArray {
    val outputBuffer = ByteBuffer.allocate(input.size * 2)
    outputBuffer.order(ByteOrder.nativeOrder())
    input.forEach { byte ->
        val unsignedValue = byte.toInt() and 0xFF
        val signed16Bit = (unsignedValue - 128) * 256
        outputBuffer.putShort(signed16Bit.toShort())
    }
    return outputBuffer.array()
}

// We have to use a dedicated thread to reduce the chance of losing data
// For example, the default audio recorder thread is blocked most of the time
private val VisualizerRecordHandler = HandlerThread(
    "myVisualizerRecordThread",
    Process.THREAD_PRIORITY_URGENT_AUDIO
)
    .apply { start() }
    .run { Handler(this.looper) }
private val VisualizerRecordDispatcher = VisualizerRecordHandler.asCoroutineDispatcher()
