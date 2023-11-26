package com.mrsep.musicrecognizer.feature.recognition.domain

import android.content.Context
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.OneTimeRecognitionTileService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface WidgetNotifier {

    fun requestUpdate()

}

internal class WidgetNotifierImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : WidgetNotifier {

    override fun requestUpdate() {
        OneTimeRecognitionTileService.requestListeningState(appContext)
    }

}
