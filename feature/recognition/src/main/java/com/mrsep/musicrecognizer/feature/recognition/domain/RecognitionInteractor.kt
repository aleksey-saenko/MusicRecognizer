package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface RecognitionInteractor {

    val statusFlow: StateFlow<RecognitionStatus>

    fun launchRecognitionOrCancel(scope: CoroutineScope)

    fun cancelRecognition()
    suspend fun resetStatusToReady(addLastRecordToQueue: Boolean)

}