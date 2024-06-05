package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface RecognitionInteractor {

    val status: StateFlow<RecognitionStatus>

    fun launchRecognition(scope: CoroutineScope)

    fun launchOfflineRecognition(scope: CoroutineScope)

    fun cancelAndResetStatus()

    fun resetFinalStatus()
}
