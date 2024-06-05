package com.mrsep.musicrecognizer.data.audiorecord

import android.media.AudioFormat
import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * As the device A/D convertor has an AGC (Automatic Gain Control), so there is no direct
 * correlation with a sample value and a real decibel level.
 */
class SoundAmplitudeSourceImpl @Inject constructor(
    soundSource: SoundSource,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : SoundAmplitudeSourceDo {

    override val amplitudeFlow = (soundSource.params?.let { params ->
        check(params.audioFormat.channelCount == 1) {
            "SoundAmplitudeSourceImpl supports mono channel AudioSource only"
        }
        val relativeMaximumFlow = when (params.audioFormat.encoding) {
            AudioFormat.ENCODING_PCM_16BIT -> soundSource.pcmChunkFlow
                .transformWhile {pcmChunkResult ->
                    var takeWhile = true
                    pcmChunkResult.onSuccess { chunk ->
                        emit(chunk.toShortArray().getRelativeMaximum())
                    }.onFailure {
                        takeWhile = false
                    }
                    takeWhile
                }
            AudioFormat.ENCODING_PCM_FLOAT -> soundSource.pcmChunkFlow
                .transformWhile {pcmChunkResult ->
                    var takeWhile = true
                    pcmChunkResult.onSuccess { chunk ->
                        emit(chunk.toFloatArray().getRelativeMaximum())
                    }.onFailure {
                        takeWhile = false
                    }
                    takeWhile
                }
            else -> throw IllegalArgumentException("Unsupported encoding")
        }
        relativeMaximumFlow.map { relativeMaximum ->
            calculateRelativeDBFS(relativeMaximum)
        }
    } ?: emptyFlow())
        .transformToMovingAverage(MOVING_AVERAGE_WINDOW_SIZE)
        .map { float -> float.roundTo(1) }
        .distinctUntilChanged { old, new -> old.equalsDelta(new, 0.01f) }
        .flowOn(defaultDispatcher)

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

    private fun ShortArray.getRelativeMaximum(): Float {
        return maxOfOrNull { short -> short.toInt().absoluteValue }?.let { maxInChunk ->
            (maxInChunk.toFloat() / Short.MIN_VALUE).absoluteValue
        } ?: 0f
    }

    private fun FloatArray.getRelativeMaximum(): Float {
        return maxOfOrNull { float -> float.absoluteValue } ?: 0f
    }

    private fun calculateRelativeDBFS(relativeMax: Float): Float {
        val dBFS = 20 * log10(relativeMax).coerceIn(SILENCE_THRESHOLD, 0f)
        if (dBFS.isNaN()) return 0f
        return (dBFS.div(SILENCE_THRESHOLD.absoluteValue) + 1).coerceIn(0f, 1f)
    }

    private fun Flow<Float>.transformToMovingAverage(windowSize: Int) = flow {
        val chunk = ArrayDeque<Float>(windowSize)
        collect { maxAmplitude ->
            chunk.add(maxAmplitude)
            if (chunk.size == windowSize) {
                emit(chunk.sum() / windowSize)
                chunk.removeFirst()
            }
        }
    }

    private fun Float.roundTo(decimalPlaces: Int): Float {
        check(decimalPlaces > 0) { "decimalPlaces must be > 0" }
        val factor = 10f.pow(decimalPlaces)
        return (this * factor).roundToInt() / factor
    }

    private fun Float.equalsDelta(other: Float, delta: Float) = abs(this - other) < delta

    private fun FloatArray.calculateRms() =
        sqrt(fold(0f) { acc, float -> acc + float.pow(2) }.div(size))

    companion object {
        private const val SILENCE_THRESHOLD = -50f
        private const val MOVING_AVERAGE_WINDOW_SIZE = 15
    }
}
