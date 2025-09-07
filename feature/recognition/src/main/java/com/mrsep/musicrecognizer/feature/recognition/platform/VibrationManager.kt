package com.mrsep.musicrecognizer.feature.recognition.platform

internal interface VibrationManager {

    fun vibrateResult(isSuccess: Boolean)

    fun vibrateOnTap()
}
