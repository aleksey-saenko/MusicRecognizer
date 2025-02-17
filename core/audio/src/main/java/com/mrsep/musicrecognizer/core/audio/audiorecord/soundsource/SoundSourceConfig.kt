package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.media.AudioFormat

internal class SoundSourceConfig(
    val audioFormat: AudioFormat,
    val minBufferSize: Int
) {

    init {
        check(audioFormat.channelCount == 1) { "Unsupported channel count" }
    }

    val bytesPerFrame = audioFormat.encoding.toByteAllocation() * audioFormat.channelCount
    val chunkSize = calcChunkSize()
    val chunkSizeInSeconds = audioFormat.run {
        1.0 * chunkSize / (sampleRate * bytesPerFrame)
    }

    private fun calcChunkSize(): Int {
        minBufferSize.div(2).let { half ->
            half.rem(bytesPerFrame).let { rem ->
                return if (rem == 0) half else half - rem
            }
        }
    }

    private fun Int.toByteAllocation(): Int {
        return when (this) {
            AudioFormat.ENCODING_PCM_16BIT -> 2
            AudioFormat.ENCODING_PCM_FLOAT -> 4
            else -> throw IllegalArgumentException("Unsupported encoding")
        }
    }
}
