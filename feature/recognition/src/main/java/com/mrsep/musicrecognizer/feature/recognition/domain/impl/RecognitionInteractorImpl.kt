package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionServiceFactory
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme.Companion.recognitionScheme
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme.Companion.step
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.FallbackAction
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionProvider
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme.Companion.splitter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
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
    private val recorderController: AudioRecorderController,
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
        step(5.seconds)
    }

    private var recognitionJob: Job? = null

    override fun launchRecognition(scope: CoroutineScope) {
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
                recognitionService.recognize(
                    audioRecordingFlow = recordingChannel.receiveAsFlow()
                )
            }

            // send each recording step to recordingChannel, return full (last) recording or failure
            val recordProcess = async {
                recorderController.audioRecordingFlow(onlineScheme)
                    .withIndex()
                    .transformWhile { (index, result) ->
                        result.onSuccess { recording ->
                            val isExtraTry = (index >= onlineScheme.extraTryIndex)
                            if (index <= onlineScheme.lastStepIndex) {
                                recordingChannel.send(recording)
                                _status.update { RecognitionStatus.Recognizing(isExtraTry) }
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
                recordProcess.onAwait { recordingResult: Result<ByteArray> ->
                    recordingResult.exceptionOrNull()?.let { cause ->
                        RemoteRecognitionResult.Error.BadRecording(cause = cause)
                    } ?: remoteResult.await()
                }
            }
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
                    val trackWithStoredProps = trackRepository.upsertKeepProperties(result.track)[0]
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

    override fun launchOfflineRecognition(scope: CoroutineScope) {
        launchRecognitionIfPreviousCompleted(scope) {
            _status.update { RecognitionStatus.Recognizing(false) }
            val fullRecording = recorderController.audioRecordingFlow(offlineScheme).firstOrNull()
                ?: Result.failure(IllegalStateException("Empty audio recording flow"))
            fullRecording.onSuccess { recording ->
                val task = enqueueRecognition(recording, true)
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

    override fun cancelAndResetStatus() {
        if (recognitionJob?.isCompleted == true) {
            _status.update { RecognitionStatus.Ready }
        } else {
            recognitionJob?.cancel()
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
        if (recognitionJob == null || recognitionJob?.isCompleted == true) {
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
            enqueuedRecognitionScheduler.enqueue(recognitionId, forceLaunch = false)
        }
        return RecognitionTask.Created(recognitionId, launched)
    }

    // depending on the selected policy, waits for a full recording and creates a RecognitionTask
    // or cancels the recording job
    private suspend fun handleEnqueuedRecognition(
        fallbackAction: FallbackAction,
        recordProcess: Deferred<Result<ByteArray>>
    ): RecognitionTask {
        val (saveEnqueued, launchEnqueued) = fallbackAction
        return if (saveEnqueued) {
            runCatching {
                val fullRecord = recordProcess.await().getOrThrow()
                enqueueRecognition(fullRecord, launchEnqueued)
            }.getOrElse { cause ->
                RecognitionTask.Error(cause)
            }
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