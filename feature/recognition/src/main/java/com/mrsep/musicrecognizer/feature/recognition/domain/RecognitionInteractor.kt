package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface RecognitionInteractor {

    fun launchRecognition(scope: CoroutineScope)

    fun launchOfflineRecognition(scope: CoroutineScope)

    fun cancelAndResetStatus()

}

interface ScreenRecognitionInteractor : RecognitionInteractor {

    val screenRecognitionStatus: StateFlow<RecognitionStatus>

}

interface ServiceRecognitionInteractor : RecognitionInteractor {

    val serviceRecognitionStatus: StateFlow<RecognitionStatus>

}