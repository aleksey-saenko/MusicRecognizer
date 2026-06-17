package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.media.AudioFormat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class PcmChunk(
    val data: ByteArray,
    val audioFormat: AudioFormat,
    // Index of the first audio frame in this chunk relative to anchorInstant
    val firstFrameIndex: Long,
    val anchorInstant: Instant,
) {

    init {
        require(audioFormat.channelCount > 0) { "audioFormat must have at least one channel" }
        require(firstFrameIndex >= 0) { "firstFrameIndex must be non-negative" }
        require(data.isNotEmpty()) { "PCM chunk must not be empty" }
    }

    val frameSizeBytes: Int = audioFormat.frameSizeInBytes()
    val frameCount: Int = data.size / frameSizeBytes
    val duration: Duration = (frameCount.toDouble() / audioFormat.sampleRate).seconds
    val startInstant: Instant = anchorInstant + (firstFrameIndex.toDouble() / audioFormat.sampleRate).seconds
    val endInstant: Instant = startInstant + duration
}
