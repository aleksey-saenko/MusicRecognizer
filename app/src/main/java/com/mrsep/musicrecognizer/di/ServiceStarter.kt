package com.mrsep.musicrecognizer.di

import android.content.Context
import com.mrsep.musicrecognizer.feature.preferences.RecognitionServiceStarter
import com.mrsep.musicrecognizer.feature.recognition.service.RecognitionControlService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ServiceStarter @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : RecognitionServiceStarter {

    override fun startServiceHoldMode() {
        RecognitionControlService.startHoldMode(appContext, false)
    }

    override fun stopServiceHoldMode() {
        RecognitionControlService.stopHoldMode(appContext)
    }
}