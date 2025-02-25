package com.mrsep.musicrecognizer.feature.recognition.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles the disabling of the recognition control service when:
 * - User presses the "Disable" notification button.
 * - User dismisses the status notification (ready/listening state) on Android 14+.
 *
 * Uses a manifest-declared receiver because a detached notification can persist independently
 * of the service and app process (e.g., after reboots).
 * */
@AndroidEntryPoint
class DisableRecognitionControlServiceReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        appScope.launch {
            preferencesRepository.setNotificationServiceEnabled(false)
            ServiceNotificationHelper(context).cancelDetachedReadyStatus()
            RecognitionControlService.stopServiceGracefully(context)
        }.invokeOnCompletion {
            pendingResult.finish()
        }
    }

    companion object {

        fun intent(context: Context): Intent {
            return Intent(context, DisableRecognitionControlServiceReceiver::class.java)
        }

        fun pendingIntent(context: Context): PendingIntent {
            return PendingIntent.getBroadcast(
                context,
                0,
                intent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
