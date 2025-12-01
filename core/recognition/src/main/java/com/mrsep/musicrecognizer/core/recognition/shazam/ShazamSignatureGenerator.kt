package com.mrsep.musicrecognizer.core.recognition.shazam

import java.io.File

internal interface ShazamSignatureGenerator {
    suspend  fun generate(sample: File): Result<String>
}
