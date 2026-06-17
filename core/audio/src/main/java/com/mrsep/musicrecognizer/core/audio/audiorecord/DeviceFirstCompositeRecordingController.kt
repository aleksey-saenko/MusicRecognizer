package com.mrsep.musicrecognizer.core.audio.audiorecord

import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AudioRecordingDataSource
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.zip
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeviceFirstCompositeRecordingController(
    microphoneSoundSource: PrerecordingSoundSource,
    deviceSoundSource: PrerecordingSoundSource,
    audioRecordingDataSource: AudioRecordingDataSource,
) : AudioRecordingSessionFactory {

    private val microphoneController = CompositeRecordingController(
        soundSource = microphoneSoundSource,
        audioRecordingDataSource = audioRecordingDataSource,
    )
    private val deviceController = CompositeRecordingController(
        soundSource = deviceSoundSource,
        audioRecordingDataSource = audioRecordingDataSource,
    )

    context(scope: CoroutineScope)
    override fun startRecordingSession(scheme: RecordingScheme, includeBuffered: Boolean): AudioRecordingSession {
        val microphoneSession = microphoneController.startRecordingSession(scheme, includeBuffered)
        val deviceSession = deviceController.startRecordingSession(scheme, includeBuffered)

        return object : AudioRecordingSession {
            override val recordings = scope.produce(capacity = Channel.UNLIMITED) {
                try {
                    microphoneSession.recordings.receiveAsFlow()
                        .zip(deviceSession.recordings.receiveAsFlow()) { micRecording, devRecording ->
                            val minSignificantDuration = minOf(2.seconds, micRecording.nonSilenceDuration)
                            if (devRecording.nonSilenceDuration > minSignificantDuration) {
                                devRecording
                            } else {
                                micRecording
                            }
                        }.collect { value ->
                            send(value)
                        }
                } catch (e: Exception) {
                    ensureActive()
                    close(e)
                }
            }

            override suspend fun cancelAndDeleteSessionFiles(): Unit = coroutineScope {
                awaitAll(
                    async { microphoneSession.cancelAndDeleteSessionFiles() },
                    async { deviceSession.cancelAndDeleteSessionFiles() }
                )
            }
        }
    }
}
