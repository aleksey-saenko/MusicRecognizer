package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.core.domain.preferences.FallbackAction
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionInteractor
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionServiceFactory
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionTask
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
internal class RecognitionInteractorImpl @Inject constructor(
    private val recognitionServiceFactory: RecognitionServiceFactory,
    private val preferencesRepository: PreferencesRepository,
    private val trackRepository: TrackRepository,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val enqueuedRecognitionScheduler: EnqueuedRecognitionScheduler,
    private val trackMetadataEnhancerScheduler: TrackMetadataEnhancerScheduler,
) : RecognitionInteractor {

    private val _status = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)
    override val status = _status.asStateFlow()

    private val onlineScheme = RecordingScheme(
        steps = listOf(
            RecordingScheme.Step(recordings = listOf(4.seconds, 8.seconds)),
            RecordingScheme.Step(recordings = listOf(7.seconds))
        ),
        fallback = 10.seconds
    )

    private val offlineScheme = RecordingScheme(
        steps = listOf(RecordingScheme.Step(recordings = listOf(10.seconds)))
    )

    private var recognitionJob: Job? = null
    private val isRecognitionJobCompleted get() = recognitionJob?.isCompleted ?: true

    override fun launchRecognition(scope: CoroutineScope, recordingController: AudioRecordingController) {
        launchRecognitionIfPreviousCompleted(scope) {
            _status.update { RecognitionStatus.Recognizing(false) }
            val userPreferences = preferencesRepository.userPreferencesFlow.first()

            val serviceConfig = when (userPreferences.currentRecognitionProvider) {
                RecognitionProvider.Audd -> userPreferences.auddConfig
                RecognitionProvider.AcrCloud -> userPreferences.acrCloudConfig
            }
            val recognitionService = recognitionServiceFactory.getService(serviceConfig)

            val recordingChannel = Channel<AudioRecording>(Channel.UNLIMITED)
            val fallbackRecordingChannel = Channel<AudioRecording>(Channel.CONFLATED)
            val remoteRecognitionResult = async {
                recognitionService.recognizeFirst(recordingChannel.receiveAsFlow())
            }

            val extraTimeNotifier = launch {
                val extraTime = onlineScheme.run { steps.first().recordings.last() + 3.5.seconds }
                delay(extraTime)
                _status.update { RecognitionStatus.Recognizing(true) }
            }

            val recordResult = async {
                recordingController.audioRecordingFlow(onlineScheme)
                    .transform { result ->
                        result.onSuccess { recording ->
                            if (recording.isFallback) {
                                fallbackRecordingChannel.send(recording)
                                fallbackRecordingChannel.close()
                            } else if (recording.nonSilenceDuration > 0.5.seconds) {
                                recordingChannel.send(recording)
                            }
                        }.onFailure { cause ->
                            fallbackRecordingChannel.close(cause)
                            emit(cause)
                        }
                    }.onCompletion {
                        recordingChannel.close()
                        fallbackRecordingChannel.close()
                    }.firstOrNull()
            }

            val result: RemoteRecognitionResult = select {
                remoteRecognitionResult.onAwait { it }
                recordResult.onAwait { cause: Throwable? ->
                    if (cause == null) {
                        remoteRecognitionResult.await()
                    } else {
                        remoteRecognitionResult.cancel()
                        RemoteRecognitionResult.Error.BadRecording(cause = cause)
                    }
                }
            }
            extraTimeNotifier.cancelAndJoin()
            val recognitionResult = when (result) {
                is RemoteRecognitionResult.Error.BadRecording -> {
                    RecognitionStatus.Done(RecognitionResult.Error(result, RecognitionTask.Ignored))
                }

                is RemoteRecognitionResult.Error.BadConnection -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.fallbackPolicy.badConnection,
                        fallbackRecordingChannel
                    )
                    RecognitionStatus.Done(RecognitionResult.Error(result, recognitionTask))
                }

                is RemoteRecognitionResult.Error -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.fallbackPolicy.anotherFailure,
                        fallbackRecordingChannel
                    )
                    RecognitionStatus.Done(RecognitionResult.Error(result, recognitionTask))
                }

                RemoteRecognitionResult.NoMatches -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.fallbackPolicy.noMatches,
                        fallbackRecordingChannel
                    )
                    RecognitionStatus.Done(RecognitionResult.NoMatches(recognitionTask))
                }

                is RemoteRecognitionResult.Success -> {
                    recordResult.cancelAndJoin()
                    val trackWithStoredProps = trackRepository
                        .upsertKeepProperties(listOf(result.track))
                        .first()
                    trackRepository.setViewed(trackWithStoredProps.id, false)
                    val updatedTrack = trackWithStoredProps.copy(
                        properties = trackWithStoredProps.properties.copy(isViewed = false)
                    )
                    trackMetadataEnhancerScheduler.enqueue(updatedTrack.id)
                    RecognitionStatus.Done(RecognitionResult.Success(updatedTrack))
                }
            }
            recordResult.cancelAndJoin()
            _status.update { recognitionResult }
        }
    }

    override fun launchOfflineRecognition(scope: CoroutineScope, recordingController: AudioRecordingController) {
        launchRecognitionIfPreviousCompleted(scope) {
            _status.update { RecognitionStatus.Recognizing(false) }
            val recordingResult = recordingController.audioRecordingFlow(offlineScheme)
                .firstOrNull()
                ?: Result.failure(IllegalStateException("Empty audio recording flow"))
            val result = recordingResult.fold(
                onSuccess = { recording ->
                    val task = enqueueRecognition(recording, true)
                    RecognitionResult.ScheduledOffline(task)
                },
                onFailure = { cause ->
                    RecognitionResult.Error(
                        RemoteRecognitionResult.Error.BadRecording(cause = cause),
                        RecognitionTask.Ignored
                    )
                }
            )
            _status.update { RecognitionStatus.Done(result) }
        }
    }

    override suspend fun cancelAndJoin() {
        if (isRecognitionJobCompleted) {
            _status.update { RecognitionStatus.Ready }
        } else {
            recognitionJob?.cancelAndJoin()
        }
    }

    override fun resetFinalStatus() {
        _status.update { oldStatus ->
            if (oldStatus is RecognitionStatus.Done) RecognitionStatus.Ready else oldStatus
        }
    }

    private fun launchRecognitionIfPreviousCompleted(
        scope: CoroutineScope,
        block: suspend CoroutineScope.() -> Unit
    ) {
        if (isRecognitionJobCompleted) {
            recognitionJob = scope.launch(block = block).setCancellationHandler()
        }
    }

    private suspend fun enqueueRecognition(
        audioRecording: AudioRecording,
        launched: Boolean
    ): RecognitionTask {
        val recognitionId = enqueuedRecognitionRepository.createRecognition(
            audioRecording = audioRecording,
            title = ""
        )
        recognitionId ?: return RecognitionTask.Error()
        if (launched) {
            enqueuedRecognitionScheduler.enqueue(listOf(recognitionId), forceLaunch = false)
        }
        return RecognitionTask.Created(recognitionId, launched)
    }

    private suspend fun handleEnqueuedRecognition(
        fallbackAction: FallbackAction,
        fallbackRecordingChannel: ReceiveChannel<AudioRecording>,
    ): RecognitionTask {
        val (saveEnqueued, launchEnqueued) = fallbackAction
        var recognitionTask: RecognitionTask = RecognitionTask.Ignored
        if (saveEnqueued) {
            fallbackRecordingChannel.receiveCatching().onSuccess { recording ->
                recognitionTask = enqueueRecognition(recording, launchEnqueued)
            }.onFailure { cause ->
                recognitionTask = RecognitionTask.Error(cause)
            }
        }
        return recognitionTask
    }

    private fun Job.setCancellationHandler() = apply {
        invokeOnCompletion { cause ->
            when (cause) {
                is CancellationException -> _status.update { RecognitionStatus.Ready }
            }
        }
    }
}
