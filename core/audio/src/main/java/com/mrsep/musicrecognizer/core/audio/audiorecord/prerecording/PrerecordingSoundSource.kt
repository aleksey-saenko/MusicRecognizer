package com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording

import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSourceConfig
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.PcmChunk
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

internal interface PrerecordingSoundSource {
    val key: SoundSourceKey
    val audioSource: AudioSource
    val params: SoundSourceConfig?
    val soundLevel: StateFlow<Float>
    val state: StateFlow<PrerecordingSoundSourceState>

    suspend fun startPrerecording(bufferDuration: Duration): Boolean
    suspend fun stopPrerecording()
    suspend fun clearBuffer()

    fun captureFlow(includeBuffered: Boolean = true): Flow<Result<PcmChunk>>

    suspend fun close()
}
