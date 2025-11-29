package com.mrsep.signature

internal object ShazamSignature {

    init {
        System.loadLibrary("")
    }

    const val REQUIRED_SAMPLE_RATE = 16_000

    /**
     * accepts a ByteArray (mono, PCM int16, 16_000 Hz),
     * returns the encoded signature string or throw exception
     */
    @JvmStatic
    external fun fromI16(samples: ByteArray): String
}
