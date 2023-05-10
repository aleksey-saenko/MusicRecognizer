package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionResultDelegator
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.ScreenRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
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
    private val resultDelegator: RecognitionResultDelegator
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
                            emit(RemoteRecognitionResult.Error.BadRecording)
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
                    RecognitionStatus.Done(
                        RecognitionResult.Error(result, ByteArray(0))
                    )
                }

                is RemoteRecognitionResult.Error -> {
                    val wrappedResult =
                        fullRecordingChannel.wrappedOrBadRecordingResult { fullRecord ->
                            RecognitionResult.Error(result, fullRecord)
                        }
                    RecognitionStatus.Done(wrappedResult)
                }

                RemoteRecognitionResult.NoMatches -> {
                    val wrappedResult =
                        fullRecordingChannel.wrappedOrBadRecordingResult { fullRecord ->
                            RecognitionResult.NoMatches(fullRecord)
                        }
                    RecognitionStatus.Done(wrappedResult)
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

    private suspend fun ReceiveChannel<ByteArray>.wrappedOrBadRecordingResult(
        wrapper: (ByteArray) -> RecognitionResult
    ): RecognitionResult {
        return runCatching {
            val fullRecord = receive()
            wrapper(fullRecord)
        }.getOrElse { cause ->
            RecognitionResult.Error(
                RemoteRecognitionResult.Error.BadRecording,
                ByteArray(0)
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
            println("CancellationHandler invoked, cause=$cause")
            when (cause) {
                is CancellationException -> {
                    recognitionJob = null
                    resultDelegator.notify(RecognitionStatus.Ready)
                }
            }
        }
    }


    companion object {
        private const val extraTryStartIndex = 3

        private val strategy = AudioRecordingStrategy.Builder()
//            .addStep(3_500.milliseconds)
            .addStep(6_000.milliseconds)
            .addStep(9_000.milliseconds)
            .addSplitter()
            .addStep(12_500.milliseconds)
            .addStep(15_000.milliseconds)
//            .addStep(18_000.milliseconds)
            .sendTotalAtEnd(true)
            .build()
//        private val strategy = AudioRecordingStrategy.Builder()
//            .addStep(6_000.milliseconds)
//            .addStep(10_000.milliseconds)
//            .addStep(18_000.milliseconds)
//            .sendTotalAtEnd(true)
//            .build()
    }

}