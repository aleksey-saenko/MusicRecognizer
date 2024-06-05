package com.mrsep.musicrecognizer.feature.recognition.domain

internal interface VibrationManager {

    fun vibrateResult(isSuccess: Boolean)

    fun vibrateOnTap()
}
