package com.mrsep.musicrecognizer.core.audio.audiorecord

import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AudioRecordingDataSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.Mp4RecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.WavRecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingSession
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow

@OptIn(ExperimentalCoroutinesApi::class)
internal class CompositeRecordingController(
    soundSource: SoundSource,
    audioRecordingDataSource: AudioRecordingDataSource,
) : AudioRecordingController {

    private val mp4Controller = Mp4RecordingController(soundSource, audioRecordingDataSource)
    private val wavController = WavRecordingController(soundSource, audioRecordingDataSource)

    override val soundLevel = soundSource.soundLevel

    context(scope: CoroutineScope)
    override fun startRecordingSession(scheme: RecordingScheme): AudioRecordingSession = when {

        scheme.encodeSteps -> mp4Controller.startRecordingSession(scheme)

        scheme.fallback == null -> wavController.startRecordingSession(scheme)

        else -> {
            val stepsSession = wavController.startRecordingSession(scheme.copy(fallback = null))
            val fallbackSession = mp4Controller.startRecordingSession(scheme.copy(steps = emptyList()))

            object : AudioRecordingSession {
                override val recordings = scope.produce(capacity = Channel.UNLIMITED) {
                    try {
                        merge(
                            stepsSession.recordings.receiveAsFlow(),
                            fallbackSession.recordings.receiveAsFlow()
                        ).collect { value ->
                            send(value)
                        }
                    } catch (e: Exception) {
                        ensureActive()
                        close(e)
                    }
                }

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