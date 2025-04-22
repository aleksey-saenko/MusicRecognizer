package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val TAG = "SoundLevelMeter"

internal class SoundLevelMeter(
    params: SoundSourceConfig? = SoundSourceConfigProvider.config,
) {

    private val chunkEnergyChannel = Channel<Double>(Channel.CONFLATED)

    fun processNewChunk(pcmChunk: ShortArray) {
        chunkEnergyChannel.trySend(pcmChunk.calculateChunkEnergy())
    }

    fun processNewChunk(pcmChunk: FloatArray) {
        chunkEnergyChannel.trySend(pcmChunk.calculateChunkEnergy())
    }

    val soundLevel: StateFlow<Float> = if (params != null) {
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
        private const val SILENCE_THRESHOLD_DECIBELS = -60f
        private const val MOVING_WINDOW_MILLISECONDS = 400
    }
}

private fun Float.roundTo(decimalPlaces: Int): Float {
    check(decimalPlaces > 0) { "decimalPlaces must be > 0" }
    val factor = 10f.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor
}

private fun Float.equalsDelta(other: Float, delta: Float) = abs(this - other) < delta
