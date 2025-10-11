package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.media.AudioFormat

internal class SoundSourceConfig(
    val audioFormat: AudioFormat,
    val minBufferSize: Int
) {

    init { check(audioFormat.channelCount == 1) { "Unsupported channel count" } }

    val bytesPerFrame = audioFormat.encoding.toByteAllocation() * audioFormat.channelCount
    val chunkSize = calcChunkSize()
    val chunkSizeInSeconds = audioFormat.run { chunkSize * 1.0 / (sampleRate * bytesPerFrame) }

    private fun calcChunkSize(): Int {
        // Since we use the AAC codec, request a chunk size equal to a single AAC frame (1024 samples per channel)
        return 1024 * bytesPerFrame // ~21.333 ms at 48 kHz mono
    }

    private fun Int.toByteAllocation(): Int {
        return when (this) {
            AudioFormat.ENCODING_PCM_16BIT -> 2
            AudioFormat.ENCODING_PCM_FLOAT -> 4
            else -> error("Unsupported encoding")
        }
    }
}
