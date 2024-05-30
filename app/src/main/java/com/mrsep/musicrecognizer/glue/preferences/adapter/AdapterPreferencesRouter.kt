package com.mrsep.musicrecognizer.glue.preferences.adapter

import android.content.Context
import android.content.Intent
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRouter
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AdapterPreferencesRouter @Inject constructor(
    @ApplicationContext private val appContext: Context
) : PreferencesRouter {

    override fun startServiceHoldMode() {
        appContext.startService(
            Intent(appContext, NotificationService::class.java)
                .setAction(NotificationService.HOLD_MODE_ON_ACTION)
                .putExtra(NotificationService.KEY_RESTRICTED_START, false)
        )
    }

    override fun stopServiceHoldMode() {
        appContext.startService(
            Intent(appContext, NotificationService::class.java)
                .setAction(NotificationService.HOLD_MODE_OFF_ACTION)
        )
    }
}