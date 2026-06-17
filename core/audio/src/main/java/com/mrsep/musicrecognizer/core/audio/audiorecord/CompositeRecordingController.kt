package com.mrsep.musicrecognizer.core.audio.audiorecord

import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AudioRecordingDataSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.Mp4RecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.WavRecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording.PrerecordingSoundSource
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
    soundSource: PrerecordingSoundSource,
    audioRecordingDataSource: AudioRecordingDataSource,
) : AudioRecordingSessionFactory {

    private val mp4Controller = Mp4RecordingController(soundSource, audioRecordingDataSource)
    private val wavController = WavRecordingController(soundSource, audioRecordingDataSource)

    context(scope: CoroutineScope)
    override fun startRecordingSession(scheme: RecordingScheme, includeBuffered: Boolean): AudioRecordingSession = when {
        scheme.encodeSteps -> mp4Controller.startRecordingSession(scheme, includeBuffered)
        scheme.fallback == null -> wavController.startRecordingSession(scheme, includeBuffered)
        else -> {
            val stepsSession = wavController.startRecordingSession(scheme.copy(fallback = null), includeBuffered)
            val fallbackSession = mp4Controller.startRecordingSession(scheme.copy(steps = emptyList()), includeBuffered)

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
