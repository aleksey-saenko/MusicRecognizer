package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface RecognitionInteractor {

    val status: StateFlow<RecognitionStatus>

    fun launchRecognition(scope: CoroutineScope, recordingController: AudioRecordingController)

    fun launchOfflineRecognition(scope: CoroutineScope, recordingController: AudioRecordingController)

    suspend fun cancelAndJoin()

    fun resetFinalStatus()
}
