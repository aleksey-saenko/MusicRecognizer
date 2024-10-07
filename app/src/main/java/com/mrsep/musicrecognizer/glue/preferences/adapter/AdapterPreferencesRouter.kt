package com.mrsep.musicrecognizer.glue.preferences.adapter

import android.content.Context
import android.content.Intent
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRouter
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlService
import com.mrsep.musicrecognizer.presentation.MainActivity.Companion.restartApplicationOnRestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AdapterPreferencesRouter @Inject constructor(
    @ApplicationContext private val appContext: Context
) : PreferencesRouter {

    override fun startServiceHoldMode() {
        appContext.startService(
            Intent(appContext, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_HOLD_MODE_ON)
                .putExtra(RecognitionControlService.KEY_RESTRICTED_START, false)
        )
    }

    override fun stopServiceHoldMode() {
        appContext.startService(
            Intent(appContext, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_HOLD_MODE_OFF)
        )
    }

    override fun restartApplicationOnRestore() {
        appContext.restartApplicationOnRestore()
    }
}
