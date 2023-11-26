package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * This transparent activity can solve these problems:
 * 1 - Allows service microphone access.
 * Foreground service started from background (receiver on_boot_completed for example, not activity)
 * doesn't have access to the microphone.
 * https://developer.android.com/guide/components/foreground-services#bg-access-restrictions
 * 2 - Foreground service doesn't allow to execute any additional code before launch activity
 * after user click (pending intent as action). This activity does.
 * Can be used to reset interactor status before open track screen.
 * https://developer.android.com/about/versions/12/behavior-changes-12#notification-trampolines
 */

@AndroidEntryPoint
class NotificationServiceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent.action) {
            ACTION_ONE_TIME_RECOGNITION -> {
                startNotificationService(oneTimeRecognition = true)
            }

            else -> {}
        }
        finish()
    }

    companion object {
        const val ACTION_ONE_TIME_RECOGNITION = "ACTION_ONE_TIME_RECOGNITION"
    }

}