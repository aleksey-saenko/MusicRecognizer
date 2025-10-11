package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface RecognitionInteractor {

    val status: StateFlow<RecognitionStatus>

    context(scope: CoroutineScope)
    fun launchRecognition(recordingController: AudioRecordingController)

    suspend fun cancelAndJoin()

    fun resetFinalStatus()
}
