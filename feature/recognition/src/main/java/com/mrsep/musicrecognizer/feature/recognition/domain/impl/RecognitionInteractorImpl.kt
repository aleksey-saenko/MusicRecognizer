package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionResultDelegator
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.ScreenRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy.Companion.audioRecognitionStrategy
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy.Companion.splitter
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy.Companion.step
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduleAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
private const val TAG = "RecognitionInteractorImpl"

@Singleton
class RecognitionInteractorImpl @Inject constructor(
    private val recorderController: AudioRecorderController,
    private val recognitionService: RemoteRecognitionService,
    private val preferencesRepository: PreferencesRepository,
    private val trackRepository: TrackRepository,
    private val resultDelegator: RecognitionResultDelegator,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository
) : ScreenRecognitionInteractor, ServiceRecognitionInteractor {

    override val screenRecognitionStatus get() = resultDelegator.screenState
    override val serviceRecognitionStatus get() = resultDelegator.serviceState

    private val onlineStrategy = audioRecognitionStrategy(true) {
        step(3_500.milliseconds)
        step(6_000.milliseconds)
        step(9_000.milliseconds)
        splitter(true)
        step(12_500.milliseconds)
        step(15_000.milliseconds)
        step(18_000.milliseconds)
    }

    private val offlineStrategy = audioRecognitionStrategy(false) {
        step(10.seconds)
    }

    private var recognitionJob: Job? = null

    private fun launchRecognitionIfPreviousCompleted(
        scope: CoroutineScope,
        block: suspend CoroutineScope.() -> Unit
    ) {
        if (recognitionJob == null || recognitionJob?.isCompleted == true) {
            recognitionJob = scope.launch(block = block).setCancellationHandler()
        }
    }

    override fun launchRecognition(scope: CoroutineScope) {
        launchRecognitionIfPreviousCompleted(scope) {
            resultDelegator.notify(RecognitionStatus.Recognizing(false))
            val userPreferences = preferencesRepository.userPreferencesFlow.first()

            val eachRecordingChannel = Channel<ByteArray>(onlineStrategy.steps.size)
            val remoteResult = async {
                recognitionService.recognize(
                    token = userPreferences.apiToken,
                    requiredServices = userPreferences.requiredServices,
                    audioRecordingFlow = eachRecordingChannel.receiveAsFlow()
                )
            }

            // send each recording step to eachRecordingChannel, return full (last) recording or failure
            val recordingAsync = async {
                recorderController.audioRecordingFlow(onlineStrategy)
                    .withIndex()
                    .transformWhile { (index, result) ->
                        result.onSuccess { recording ->
                            val isExtraTry = (index >= onlineStrategy.extraTryIndex)
                            if (index <= onlineStrategy.lastStepIndex) {
                                eachRecordingChannel.send(recording)
                                resultDelegator.notify(RecognitionStatus.Recognizing(isExtraTry))
                            }
                        }
                        if (result.isFailure || index == onlineStrategy.lastStepIndex) {
                            eachRecordingChannel.close()
                        }
                        emit(result)
                        result.isSuccess && index <= onlineStrategy.lastRecordingIndex
                    }.last()
            }

            val result = select {
                remoteResult.onAwait { remoteRecognitionResult -> remoteRecognitionResult }
                recordingAsync.onAwait { recordingResult: Result<ByteArray> ->
                    recordingResult.exceptionOrNull()?.let { cause ->
                        RemoteRecognitionResult.Error.BadRecording(cause)
                    } ?: remoteResult.await()
                }
            }

            val recognitionResult = when (result) {
                is RemoteRecognitionResult.Error.BadRecording -> {
                    // bad recording result can be sent by server or by recording job,
                    // so we should stop both job manually
                    remoteResult.cancelAndJoin()
                    recordingAsync.cancelAndJoin()
                    RecognitionStatus.Done(RecognitionResult.Error(result, RecognitionTask.Ignored))
                }

                is RemoteRecognitionResult.Error.BadConnection -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.schedulePolicy.badConnection,
                        recordingAsync
                    )
                    RecognitionStatus.Done(RecognitionResult.Error(result, recognitionTask))
                }

                is RemoteRecognitionResult.Error -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.schedulePolicy.anotherFailure,
                        recordingAsync
                    )
                    RecognitionStatus.Done(RecognitionResult.Error(result, recognitionTask))
                }

                RemoteRecognitionResult.NoMatches -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.schedulePolicy.noMatches,
                        recordingAsync
                    )
                    RecognitionStatus.Done(RecognitionResult.NoMatches(recognitionTask))
                }

                is RemoteRecognitionResult.Success -> {
                    recordingAsync.cancelAndJoin()
                    val newTrack =
                        trackRepository.insertOrReplaceSaveMetadata(result.track).first()
                    RecognitionStatus.Done(RecognitionResult.Success(newTrack))
                }
            }
            resultDelegator.notify(recognitionResult)
        }
    }

    private suspend fun enqueueRecognition(
        audioRecord: ByteArray,
        launch: Boolean
    ): RecognitionTask {
        return enqueuedRecognitionRepository.createEnqueuedRecognition(
            audioRecord,
            launch
        )?.let { enqueuedId ->
            RecognitionTask.Created(enqueuedId, launch)
        } ?: RecognitionTask.Error()
    }

    // depending on the selected policy, waits for a full recording and creates a RecognitionTask
    // or cancels the recording job
    private suspend fun handleEnqueuedRecognition(
        scheduleAction: ScheduleAction,
        fullRecordingAsync: Deferred<Result<ByteArray>>
    ): RecognitionTask {
        val (saveEnqueued, launchEnqueued) = scheduleAction
        return if (saveEnqueued) {
            runCatching {
                val fullRecord = fullRecordingAsync.await().getOrThrow()
                enqueueRecognition(fullRecord, launchEnqueued)
            }.getOrElse { cause ->
                RecognitionTask.Error(cause)
            }
        } else {
            fullRecordingAsync.cancelAndJoin()
            RecognitionTask.Ignored
        }
    }

    override fun launchOfflineRecognition(scope: CoroutineScope) {
        launchRecognitionIfPreviousCompleted(scope) {
            resultDelegator.notify(RecognitionStatus.Recognizing(false))
            val fullRecording = recorderController.audioRecordingFlow(offlineStrategy).firstOrNull()
                ?: Result.failure(IllegalStateException("Empty audio recording flow"))
            fullRecording.onSuccess { recording ->
                val task = enqueueRecognition(recording, true)
                val result = RecognitionResult.ScheduledOffline(task)
                resultDelegator.notify(RecognitionStatus.Done(result))
            }.onFailure { cause ->
                val error = RecognitionResult.Error(
                    RemoteRecognitionResult.Error.BadRecording(cause),
                    RecognitionTask.Ignored
                )
                resultDelegator.notify(RecognitionStatus.Done(error))
            }
        }
    }

    override fun cancelAndResetStatus() {
        if (recognitionJob?.isCompleted == true) {
            resultDelegator.notify(RecognitionStatus.Ready)
        } else {
            recognitionJob?.cancel()
        }
    }

    private fun Job.setCancellationHandler() = apply {
        invokeOnCompletion { cause ->
            when (cause) {
                is CancellationException -> resultDelegator.notify(RecognitionStatus.Ready)
            }
        }
    }

}