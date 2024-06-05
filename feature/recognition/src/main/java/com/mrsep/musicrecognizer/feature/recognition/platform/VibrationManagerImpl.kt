package com.mrsep.musicrecognizer.feature.recognition.platform

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import com.mrsep.musicrecognizer.core.common.util.getDefaultVibrator
import com.mrsep.musicrecognizer.feature.recognition.domain.VibrationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class VibrationManagerImpl @Inject constructor(
    @ApplicationContext appContext: Context
) : VibrationManager {

    private val vibrator = appContext.getDefaultVibrator()

    private val successEffect by lazy {
        val timings = longArrayOf(90)
        val amplitudes = intArrayOf(-1)
        VibrationEffect.createWaveform(timings, amplitudes, -1)
    }

    private val failureEffect by lazy {
        val timings = longArrayOf(50, 90, 50)
        val amplitudes = intArrayOf(-1, 0, -1)
        VibrationEffect.createWaveform(timings, amplitudes, -1)
    }

    private val tapEffect by lazy {
        val timings = longArrayOf(50)
        val amplitudes = intArrayOf(-1)
        VibrationEffect.createWaveform(timings, amplitudes, -1)
    }

    override fun vibrateResult(isSuccess: Boolean) {
        if (!vibrator.hasVibrator()) return
        vibrate(if (isSuccess) successEffect else failureEffect)
    }

    override fun vibrateOnTap() {
        if (!vibrator.hasVibrator()) return
        vibrate(tapEffect)
    }

    @SuppressLint("MissingPermission")
    private fun vibrate(vibrationEffect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val vibrationAttributes = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_MEDIA)
                .build()
            vibrator.vibrate(vibrationEffect, vibrationAttributes)
        } else {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationEffect, audioAttributes)
        }
    }
}
