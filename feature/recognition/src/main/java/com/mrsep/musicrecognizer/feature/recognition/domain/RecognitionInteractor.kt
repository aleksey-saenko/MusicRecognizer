package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface RecognitionInteractor {

    val status: StateFlow<RecognitionStatus>

    fun launchRecognition(scope: CoroutineScope, recorderController: AudioRecorderController)

    fun launchOfflineRecognition(scope: CoroutineScope, recorderController: AudioRecorderController)

    suspend fun cancelAndJoin()

    fun resetFinalStatus()
}
