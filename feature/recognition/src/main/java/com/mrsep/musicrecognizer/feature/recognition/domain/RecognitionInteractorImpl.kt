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
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionScheme.Companion.recognitionScheme
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionScheme.Companion.splitter
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionScheme.Companion.step
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionTask
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
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

    private val onlineScheme = recognitionScheme(true) {
        step(4_000.milliseconds)
        step(8_000.milliseconds)
        splitter(true)
        step(15_000.milliseconds)
    }

    private val offlineScheme = recognitionScheme(false) {
        step(10.seconds)
    }

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

            val recordingChannel = Channel<ByteArray>(onlineScheme.stepCount)
            val remoteResult = async {
                recognitionService.recognize(recordingChannel.receiveAsFlow())
            }

            val extraTimeNotifier = launch {
                val extraTime = onlineScheme.run { steps[extraTryIndex].timestamp + 3.5.seconds }
                delay(extraTime)
                _status.update { RecognitionStatus.Recognizing(true) }
            }

            // send each recording step to recordingChannel, return full (last) recording or failure
            val recordProcess = async {
                recordingController.audioRecordingFlow(onlineScheme)
                    .withIndex()
                    .transformWhile { (index, result) ->
                        result.onSuccess { recording ->
                            if (recording.nonSilenceDuration < 0.5.seconds) return@onSuccess
                            if (index <= onlineScheme.lastStepIndex) {
                                recordingChannel.send(recording.data)
                            }
                        }
                        if (result.isFailure || index == onlineScheme.lastStepIndex) {
                            recordingChannel.close()
                        }
                        emit(result)
                        result.isSuccess && index <= onlineScheme.lastRecordingIndex
                    }.last()
            }

            val result = select<RemoteRecognitionResult> {
                remoteResult.onAwait { remoteRecognitionResult -> remoteRecognitionResult }
                recordProcess.onAwait { recordingResult: Result<AudioRecording> ->
                    recordingResult.exceptionOrNull()?.let { cause ->
                        RemoteRecognitionResult.Error.BadRecording(cause = cause)
                    } ?: remoteResult.await()
                }
            }
            extraTimeNotifier.cancelAndJoin()
            val recognitionResult = when (result) {
                is RemoteRecognitionResult.Error.BadRecording -> {
                    // bad recording result can be sent by server or by recording job,
                    // so we should stop both job manually
                    remoteResult.cancelAndJoin()
                    recordProcess.cancelAndJoin()
                    RecognitionStatus.Done(RecognitionResult.Error(result, RecognitionTask.Ignored))
                }

                is RemoteRecognitionResult.Error.BadConnection -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.fallbackPolicy.badConnection,
                        recordProcess
                    )
                    RecognitionStatus.Done(RecognitionResult.Error(result, recognitionTask))
                }

                is RemoteRecognitionResult.Error -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.fallbackPolicy.anotherFailure,
                        recordProcess
                    )
                    RecognitionStatus.Done(RecognitionResult.Error(result, recognitionTask))
                }

                RemoteRecognitionResult.NoMatches -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.fallbackPolicy.noMatches,
                        recordProcess
                    )
                    RecognitionStatus.Done(RecognitionResult.NoMatches(recognitionTask))
                }

                is RemoteRecognitionResult.Success -> {
                    recordProcess.cancelAndJoin()
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
            _status.update { recognitionResult }
        }
    }

    override fun launchOfflineRecognition(scope: CoroutineScope, recordingController: AudioRecordingController) {
        launchRecognitionIfPreviousCompleted(scope) {
            _status.update { RecognitionStatus.Recognizing(false) }
            val fullRecording = recordingController.audioRecordingFlow(offlineScheme).firstOrNull()
                ?: Result.failure(IllegalStateException("Empty audio recording flow"))
            fullRecording.onSuccess { recording ->
                val task = enqueueRecognition(recording.data, true)
                val result = RecognitionResult.ScheduledOffline(task)
                _status.update { RecognitionStatus.Done(result) }
            }.onFailure { cause ->
                val error = RecognitionResult.Error(
                    RemoteRecognitionResult.Error.BadRecording(cause = cause),
                    RecognitionTask.Ignored
                )
                _status.update { RecognitionStatus.Done(error) }
            }
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
        audioRecording: ByteArray,
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

    // depending on the selected policy, waits for a full recording and creates a RecognitionTask
    // or cancels the recording job
    private suspend fun handleEnqueuedRecognition(
        fallbackAction: FallbackAction,
        recordProcess: Deferred<Result<AudioRecording>>
    ): RecognitionTask {
        val (saveEnqueued, launchEnqueued) = fallbackAction
        return if (saveEnqueued) {
            recordProcess.await().fold(
                onSuccess = { fullRecording ->
                    enqueueRecognition(fullRecording.data, launchEnqueued)
                },
                onFailure = { cause ->
                    RecognitionTask.Error(cause)
                }
            )
        } else {
            recordProcess.cancelAndJoin()
            RecognitionTask.Ignored
        }
    }

    private fun Job.setCancellationHandler() = apply {
        invokeOnCompletion { cause ->
            when (cause) {
                is CancellationException -> _status.update { RecognitionStatus.Ready }
            }
        }
    }
}
