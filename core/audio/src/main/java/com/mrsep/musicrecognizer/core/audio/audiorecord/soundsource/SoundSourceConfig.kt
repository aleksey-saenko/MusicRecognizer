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
        // It seems that audio is buffered internally in chunks of minBufferSize/2, so use this to minimize latency
        val preferredSize = minBufferSize / 2
        // Round to a whole number of frames to avoid partial frames and audio corruption
        return ((preferredSize + bytesPerFrame - 1) / bytesPerFrame) * bytesPerFrame
    }

    private fun Int.toByteAllocation(): Int {
        return when (this) {
            AudioFormat.ENCODING_PCM_16BIT -> 2
            AudioFormat.ENCODING_PCM_FLOAT -> 4
            else -> error("Unsupported encoding")
        }
    }
}
