package com.mrsep.musicrecognizer.di

import android.content.Context
import android.content.Intent
import com.mrsep.musicrecognizer.feature.preferences.RecognitionServiceStarter
import com.mrsep.musicrecognizer.feature.recognition.service.RecognitionControlService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ServiceStarter @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : RecognitionServiceStarter {

    override fun startServiceHoldMode() {
        appContext.startService(
            Intent(appContext, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_HOLD_MODE_ON)
        )
    }

    override fun stopServiceHoldMode() {
        appContext.startService(
            Intent(appContext, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_HOLD_MODE_OFF)
        )
    }
}