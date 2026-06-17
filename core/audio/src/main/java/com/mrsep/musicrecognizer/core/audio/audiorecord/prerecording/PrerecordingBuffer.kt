package com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording

import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.PcmChunk
import kotlin.time.Duration

internal interface PrerecordingBuffer {
    suspend fun append(chunk: PcmChunk)
    suspend fun snapshot(): List<PcmChunk>
    suspend fun clear()
    suspend fun resize(maxDuration: Duration)
    suspend fun currentMaxDuration(): Duration
}