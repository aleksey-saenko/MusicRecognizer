package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.media.AudioFormat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class SoundSourceConfig(
    val audioFormat: AudioFormat,
    val minBufferSize: Int
) {

    init { check(audioFormat.channelCount == 1) { "Unsupported channel count" } }

    val bytesPerFrame = audioFormat.frameSizeInBytes()
    // Since we use the AAC codec, request a chunk size equal to a single AAC frame (1024 samples per channel)
    // ~21.333 ms at 48 kHz mono
    val chunkFrameCount = 1024 * audioFormat.channelCount
    val chunkSize = chunkFrameCount * bytesPerFrame
    val chunkDuration: Duration = (chunkFrameCount.toDouble() / audioFormat.sampleRate).seconds
}

internal fun AudioFormat.frameSizeInBytes(): Int {
    val bytesPerSample = when (encoding) {
        AudioFormat.ENCODING_PCM_16BIT -> 2
        AudioFormat.ENCODING_PCM_FLOAT -> 4
        else -> error("Unsupported PCM encoding: $encoding")
    }
    check(channelCount != 0)
    return bytesPerSample * channelCount
}
