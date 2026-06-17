package com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording

internal sealed interface SoundSourceKey {

    data class Microphone(val deviceId: String? = null) : SoundSourceKey

    data class DeviceOutput(val projectionTokenId: String) : SoundSourceKey
}
