package com.mrsep.musicrecognizer.data.audiorecord.soundsource

import kotlinx.coroutines.flow.SharedFlow

interface SoundSource {

    /**
     * Null value represent no any available SoundSourceConfig for this device.
     * In this case audio recording is not allowed.
     */
    val params: SoundSourceConfig?

    /**
     * All consumers should handle collected empty bytearray as error occurred
     * during audio recording.
     */
    val pcmChunkFlow: SharedFlow<Result<ByteArray>>

}