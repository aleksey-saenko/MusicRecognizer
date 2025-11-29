package com.mrsep.musicrecognizer.core.audio.audiorecord.decoder

class DecodedAudio(
    val data: ByteArray,
    val channelCount: Int,
    val sampleRate: Int,
    val pcmEncoding: Int,
)