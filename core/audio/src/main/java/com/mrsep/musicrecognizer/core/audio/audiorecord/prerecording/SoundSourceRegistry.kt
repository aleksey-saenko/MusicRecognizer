package com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording

import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource

internal interface SoundSourceRegistry {

    suspend fun getOrCreate(
        key: SoundSourceKey,
        sourceProvider: () -> SoundSource,
    ): PrerecordingSoundSource

    suspend fun getIfExists(key: SoundSourceKey): PrerecordingSoundSource?

    suspend fun close(key: SoundSourceKey)
}
