package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionResultDelegator
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.ScreenRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy
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
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "RecognitionInteractorImplNew"

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

    private var recognitionJob: Job? = null

    override fun launchRecognition(scope: CoroutineScope) {
        if (recognitionJob?.isCompleted == false) return
        recognitionJob = scope.launch {
            resultDelegator.notify(RecognitionStatus.Recognizing(false))

            val fullRecordingChannel = Channel<ByteArray>(Channel.CONFLATED)
            val eachRecordingChannel = Channel<ByteArray>(strategy.steps.size)

            val userPreferences = preferencesRepository.userPreferencesFlow.first()
            val remoteResult = async {
                recognitionService.recognize(
                    token = userPreferences.apiToken,
                    requiredServices = userPreferences.requiredServices,
                    audioRecordingFlow = eachRecordingChannel.receiveAsFlow()
                )
            }

            val recordingJob: Deferred<RemoteRecognitionResult?> = async {
                var recordingIndex = 0
                recorderController.audioRecordingFlow(strategy)
                    .transform { recordingResult ->
                        recordingResult.onFailure { cause ->
                            fullRecordingChannel.close(cause)
                            emit(RemoteRecognitionResult.Error.BadRecording(cause))
                        }.onSuccess { recording ->
                            val isExtraTry = (recordingIndex >= extraTryStartIndex)
                            when (recordingIndex) {
                                in 0..strategy.steps.lastIndex -> {
                                    eachRecordingChannel.send(recording)
                                    resultDelegator.notify(RecognitionStatus.Recognizing(isExtraTry))
                                    if (recordingIndex == strategy.steps.lastIndex && !strategy.sendTotalAtEnd) {
                                        fullRecordingChannel.send(recording)
                                    }
                                }

                                strategy.steps.lastIndex + 1 -> {
                                    if (strategy.sendTotalAtEnd) {
                                        fullRecordingChannel.send(recording)
                                    }
                                }

                                else -> throw IllegalStateException(
                                    "AudioRecorderController must not emit more than (steps.lastIndex + 1) records"
                                )
                            }
                            recordingIndex++
                        }
                    }.firstOrNull()
            }.apply {
                invokeOnCompletion {
                    fullRecordingChannel.close()
                    eachRecordingChannel.close()
                }
            }


            val result = select {
                remoteResult.onAwait { remoteRecognitionResult ->
                    remoteRecognitionResult
                }
                recordingJob.onAwait { badRecording ->
                    badRecording?.let {
                        remoteResult.cancelAndJoin()
                        badRecording
                    } ?: remoteResult.await()
                }
            }

            val recognitionResult = when (result) {
                is RemoteRecognitionResult.Error.BadRecording -> {
                    recordingJob.cancelAndJoin()
                    RecognitionStatus.Done(RecognitionResult.Error(result, RecognitionTask.Ignored))
                }

                is RemoteRecognitionResult.Error.BadConnection -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.schedulePolicy.badConnection,
                        fullRecordingChannel,
                        recordingJob
                    )
                    RecognitionStatus.Done(RecognitionResult.Error(result, recognitionTask))
                }

                is RemoteRecognitionResult.Error -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.schedulePolicy.anotherFailure,
                        fullRecordingChannel,
                        recordingJob
                    )
                    RecognitionStatus.Done(RecognitionResult.Error(result, recognitionTask))
                }

                RemoteRecognitionResult.NoMatches -> {
                    val recognitionTask = handleEnqueuedRecognition(
                        userPreferences.schedulePolicy.noMatches,
                        fullRecordingChannel,
                        recordingJob
                    )
                    RecognitionStatus.Done(RecognitionResult.NoMatches(recognitionTask))
                }

                is RemoteRecognitionResult.Success -> {
                    recordingJob.cancelAndJoin()
                    val newTrack =
                        trackRepository.insertOrReplaceSaveMetadata(result.track).first()
                    RecognitionStatus.Done(RecognitionResult.Success(newTrack))
                }
            }
            resultDelegator.notify(recognitionResult)
        }.setCancellationHandler()
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
        } ?: RecognitionTask.Error
    }

    private suspend fun handleEnqueuedRecognition(
        scheduleAction: ScheduleAction,
        fullRecordingChannel: ReceiveChannel<ByteArray>,
        recordingJob: Job
    ): RecognitionTask {
        val (saveEnqueued, launchEnqueued) = scheduleAction
        return if (saveEnqueued) {
            runCatching {
                val fullRecord = fullRecordingChannel.receive()
                enqueueRecognition(fullRecord, launchEnqueued)
            }.getOrElse { cause ->
                RecognitionTask.Error
            }
        } else {
            recordingJob.cancelAndJoin()
            RecognitionTask.Ignored
        }
    }

    private suspend fun ReceiveChannel<ByteArray>.wrappedOrBadRecordingResult(
        wrapper: suspend (ByteArray) -> RecognitionResult
    ): RecognitionResult {
        return runCatching {
            val fullRecord = receive()
            if (fullRecord.isEmpty()) throw IllegalStateException("Audio recording must not be empty")
            wrapper(fullRecord)
        }.getOrElse { cause ->
            RecognitionResult.Error(
                RemoteRecognitionResult.Error.BadRecording(cause),
                RecognitionTask.Ignored
            )
        }
    }

    override fun launchOfflineRecognition(scope: CoroutineScope) {
        TODO("Not yet implemented")
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
                is CancellationException -> {
//                    recognitionJob = null
                    resultDelegator.notify(RecognitionStatus.Ready)
                }
            }
        }
    }


    companion object {
        private const val extraTryStartIndex = 2

        private val strategy = AudioRecordingStrategy.Builder()
//            .addStep(3_500.milliseconds)
            .addStep(6_000.milliseconds)
            .addStep(9_000.milliseconds)
            .addSplitter()
//            .addStep(12_500.milliseconds)
            .addStep(15_000.milliseconds)
//            .addStep(18_000.milliseconds)
            .sendTotalAtEnd(true)
            .build()
    }

}