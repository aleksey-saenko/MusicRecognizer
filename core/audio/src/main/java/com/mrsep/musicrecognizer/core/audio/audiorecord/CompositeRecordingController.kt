package com.mrsep.musicrecognizer.core.audio.audiorecord

import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AudioRecordingDataSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.Mp4RecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.RawRecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingSession
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.receiveAsFlow

internal class CompositeRecordingController(
    soundSource: SoundSource,
    audioRecordingDataSource: AudioRecordingDataSource,
) : AudioRecordingController {

    private val mp4Controller = Mp4RecordingController(soundSource, audioRecordingDataSource)
    private val rawController = RawRecordingController(soundSource, audioRecordingDataSource)

    override val soundLevel = soundSource.soundLevel

    context(scope: CoroutineScope)
    override fun startRecordingSession(scheme: RecordingScheme): AudioRecordingSession = when {

        scheme.encodeSteps -> mp4Controller.startRecordingSession(scheme)

        scheme.fallback == null -> rawController.startRecordingSession(scheme)

        else -> {
            val stepsSession = rawController.startRecordingSession(scheme.copy(fallback = null))
            val fallbackSession = mp4Controller.startRecordingSession(scheme.copy(steps = emptyList()))

            object : AudioRecordingSession {

                override val recordings = merge(
                    stepsSession.recordings.receiveAsFlow(),
                    fallbackSession.recordings.receiveAsFlow()
                )
                    .buffer(Channel.UNLIMITED)
                    .produceIn(scope)

                override suspend fun cancelAndDeleteSessionFiles(): Unit = coroutineScope {
                    awaitAll(
                        async { stepsSession.cancelAndDeleteSessionFiles() },
                        async { fallbackSession.cancelAndDeleteSessionFiles() }
                    )
                }
            }
        }
    }
}