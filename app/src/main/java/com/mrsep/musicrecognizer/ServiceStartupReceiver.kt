package com.mrsep.musicrecognizer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Apps that target Android 12 (API level 31) or higher can't start foreground services
 * while running in the background, except for a few special cases.
 * BroadcastReceiver with ACTION_BOOT_COMPLETED is one of these.
 * https://developer.android.com/guide/components/foreground-services#background-start-restriction-exemptions
 * But a service, launched in this way, doesn't have access to microphone in easy way.
 * And we can't start transparent activity because of these restrictions:
 * https://developer.android.com/guide/components/activities/background-starts
 * */

@AndroidEntryPoint
class ServiceStartupReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            appScope.launch {
                val userPreferences = preferencesRepository.userPreferencesFlow.first()
                if (userPreferences.notificationServiceEnabled) {
                    context.startForegroundService(
                        Intent(context, RecognitionControlService::class.java)
                            .setAction(RecognitionControlService.ACTION_HOLD_MODE_ON)
                            .putExtra(RecognitionControlService.KEY_RESTRICTED_START, true)
                    )
                }
            }
        }
    }
}
