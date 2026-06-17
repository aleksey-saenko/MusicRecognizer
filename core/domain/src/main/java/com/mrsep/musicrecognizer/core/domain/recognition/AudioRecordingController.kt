package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface AudioRecordingController {

    val soundLevel: Flow<Float>

    context(scope: CoroutineScope)
    fun startRecordingSession(scheme: RecordingScheme, includeBuffered: Boolean): AudioRecordingSession

    suspend fun startPrerecording(bufferDuration: Duration)
    suspend fun stopPrerecording()
    suspend fun release()
}

interface AudioRecordingSession {

    val recordings: ReceiveChannel<AudioRecording>

    suspend fun cancelAndDeleteSessionFiles()
}
