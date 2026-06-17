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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private const val TAG = "VisualizerSoundSource"

internal class VisualizerSoundSource(
    appContext: Context,
) : SoundSource {

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

    override val pcmChunkFlow: SharedFlow<Result<PcmChunk>> = flow {
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
            Log.d(TAG, "Visualizer started")
            visualizerRef = visualizer

            val startAnchor = Clock.System.now()
            var nextFrameIndex = 0L

            val pcmChunkBuffer = PcmChunkBuffer(params.chunkSize)
            var previousWaveform: ByteArray? = null
            var lastCaptureMark = TimeSource.Monotonic.markNow()
            while (true) {
                val rawWaveform = ByteArray(visualizer.captureSize)
                // Don't use callback as its capture rate is fixed at 50ms
                visualizer.getWaveForm(rawWaveform)

                val expectedDuration = lastCaptureMark.elapsedNow()
                lastCaptureMark = TimeSource.Monotonic.markNow()

                val rawNewBytes = resolveNewWaveformBytes(
                    previousWaveform = previousWaveform,
                    rawWaveform = rawWaveform,
                    expectedDuration = expectedDuration,
                    sampleRate = params.audioFormat.sampleRate,
                )
                previousWaveform = rawWaveform

                if (rawNewBytes.isNotEmpty()) {
                    val pcm16 = convert8BitUnsignedTo16BitSigned(rawNewBytes)
                    pcmChunkBuffer.append(pcm16) { chunkBytes ->
                        soundLevelMeter.processNewChunk(chunkBytes.toShortArray())
                        val chunk = PcmChunk(
                            data = chunkBytes,
                            audioFormat = params.audioFormat,
                            firstFrameIndex = nextFrameIndex,
                            anchorInstant = startAnchor,
                        )
                        nextFrameIndex += chunk.frameCount.toLong()
                        emit(Result.success(chunk))
                    }
                }
                // If we capture too fast we get the same data (ineffective)
                // If we capture slower than 1024 samples (~20ms) we lose data
                // Selected delay 3ms empirically, but it can be adjusted later
                delay(3.milliseconds)
            }
        } finally {
            visualizerRef?.enabled = false
            visualizerRef?.release()
            Log.d(TAG, "Visualizer released")
        }
    }
        .buffer(Channel.UNLIMITED)
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

private fun resolveNewWaveformBytes(
    previousWaveform: ByteArray?,
    rawWaveform: ByteArray,
    expectedDuration: Duration,
    sampleRate: Int,
): ByteArray {
    if (previousWaveform == null) return rawWaveform
    val overlap = findOverlap(previousWaveform, rawWaveform)
    val newBytes = rawWaveform.copyOfRange(overlap, rawWaveform.size)
    if (newBytes.isNotEmpty()) return newBytes

    val expectedNewSamples = (expectedDuration.toDouble(DurationUnit.SECONDS) * sampleRate).roundToInt()
    if (isSilencedChunk(rawWaveform)) {
        return silencedChunkUnsigned8Bit(expectedNewSamples)
    }
    // The probability of this event should be extremely low
    // Increase wave capture rate otherwise
    Log.w(TAG, "New chunk repeats previous one (expectedNewSamples=$expectedNewSamples)")
    return ByteArray(0)
}

private fun findOverlap(previous: ByteArray, current: ByteArray): Int {
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

private fun isSilencedChunk(unsigned8BitSamples: ByteArray): Boolean {
    return unsigned8BitSamples.all { it.toInt() == -128 }
}

private fun silencedChunkUnsigned8Bit(expectedNewSamples: Int): ByteArray {
    return ByteArray(expectedNewSamples) { -128 }
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


private class PcmChunkBuffer(private val chunkSize: Int) {
    private val buffer = ByteArray(chunkSize)
    private var size = 0

    suspend fun append(
        newBytes: ByteArray,
        onChunkFilled: suspend (ByteArray) -> Unit,
    ) {
        var offset = 0
        while (offset < newBytes.size) {
            // Free space in buffer
            val available = chunkSize - size
            val bytesToCopy = minOf(available, newBytes.size - offset)
            System.arraycopy(
                newBytes, offset,
                buffer, size,
                bytesToCopy
            )
            size += bytesToCopy
            offset += bytesToCopy
            if (size == chunkSize) {
                onChunkFilled(buffer.copyOf())
                size = 0
            }
        }
    }
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
