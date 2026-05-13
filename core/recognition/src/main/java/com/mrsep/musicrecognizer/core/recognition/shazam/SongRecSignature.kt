package com.mrsep.musicrecognizer.core.recognition.shazam

internal object SongRecSignature {

    init {
        System.loadLibrary("songrecfp")
    }

    const val REQUIRED_SAMPLE_RATE = 16_000

    /**
     * accepts a ShortArray (mono, PCM int16, 16_000 Hz),
     * returns the encoded signature string or throw exception
     */
    @JvmStatic
    external fun fromPcm16Mono16kHz(samples: ShortArray): String
}
