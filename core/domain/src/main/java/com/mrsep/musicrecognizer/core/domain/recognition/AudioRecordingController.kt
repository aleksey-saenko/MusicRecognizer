package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

interface AudioRecordingController {

    val soundLevel: Flow<Float>

    context(scope: CoroutineScope)
    fun startRecordingSession(scheme: RecordingScheme): AudioRecordingSession
}

interface AudioRecordingSession {

    val recordings: ReceiveChannel<AudioRecording>

    suspend fun cancelAndDeleteSessionFiles()
}
