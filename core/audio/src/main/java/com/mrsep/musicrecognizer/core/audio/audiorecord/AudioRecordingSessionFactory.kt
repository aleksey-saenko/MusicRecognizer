package com.mrsep.musicrecognizer.core.audio.audiorecord

import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingSession
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineScope

internal interface AudioRecordingSessionFactory {

    context(scope: CoroutineScope)
    fun startRecordingSession(scheme: RecordingScheme, includeBuffered: Boolean): AudioRecordingSession
}
