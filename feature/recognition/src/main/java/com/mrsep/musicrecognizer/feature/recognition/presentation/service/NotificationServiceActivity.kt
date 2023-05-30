package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationService.Companion.MB_ID_EXTRA_KEY
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationService.Companion.RECOGNIZE_ACTION
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationService.Companion.SHOW_TRACK_ACTION
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This transparent activity solves these problems:
 * 1 - Allows service microphone access.
 *  Foreground service started from background (receiver on_boot_completed in my case, not activity)
 *  doesn't have access to microphone.
 *  https://developer.android.com/guide/components/foreground-services#bg-access-restrictions
 * 2 - Resets interactor status before open track screen.
 * Foreground service doesn't have access to launch activity,
 * it's allowed to set pending intent as action only (no additional logic).
 * https://developer.android.com/about/versions/12/behavior-changes-12#notification-trampolines
 */

@AndroidEntryPoint
class NotificationServiceActivity : ComponentActivity() {

    @Inject
    lateinit var recognitionInteractor: ServiceRecognitionInteractor

    @Inject
    lateinit var serviceRouter: NotificationServiceRouter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent.action) {

            RECOGNIZE_ACTION -> {
                toggleNotificationService(shouldStart = true, backgroundLaunch = false)
                sendBroadcast(Intent(RECOGNIZE_ACTION))
            }

            SHOW_TRACK_ACTION -> {
                intent.getStringExtra(MB_ID_EXTRA_KEY)?.let { trackMbId ->
                    recognitionInteractor.cancelAndResetStatus()
                    val deepLinkIntent = serviceRouter.getDeepLinkIntentToTrack(trackMbId)
                    startActivity(deepLinkIntent)
                }
            }

        }
        finish()
    }

}