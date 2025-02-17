package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.media.AudioFormat
import android.media.AudioRecord

internal object SoundSourceConfigProvider {

    val config by lazy {
        val encodings = arrayOf(AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_FLOAT)
        val sampleRates = arrayOf(48_000, 44_100, 96_000)
        val ch = AudioFormat.CHANNEL_IN_MONO
        for (enc in encodings) {
            for (rate in sampleRates) {
                val minBufferSize = AudioRecord.getMinBufferSize(rate, ch, enc)
                if (minBufferSize > 0) {
                    val format = AudioFormat.Builder()
                        .setChannelMask(ch)
                        .setEncoding(enc)
                        .setSampleRate(rate)
                        .build()
                    return@lazy SoundSourceConfig(format, minBufferSize)
                }
            }
        }
        null
    }
}
