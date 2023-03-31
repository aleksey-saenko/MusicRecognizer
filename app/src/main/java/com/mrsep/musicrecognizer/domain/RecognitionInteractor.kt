package com.mrsep.musicrecognizer.domain

import android.util.Log
import com.mrsep.musicrecognizer.di.ApplicationScope
import com.mrsep.musicrecognizer.di.DefaultDispatcher
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.domain.model.RemoteRecognitionResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecognitionInteractor @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val recognitionService: RecognitionService,
    private val trackRepository: TrackRepository,
    private val recorderController: RecorderController,
    private val preferencesRepository: PreferencesRepository,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val enqueuedRecognitionWorkManager: EnqueuedRecognitionWorkManager,
    private val fileRecordRepository: FileRecordRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    private var recognitionJob: Job? = null

    private val _statusFlow = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)
    val statusFlow = _statusFlow.asStateFlow()

    val maxAmplitudeFlow get() = recorderController.maxAmplitudeFlow


    fun launchRecognitionOrCancel(scope: CoroutineScope) {
        if (recognitionJob?.isActive == true) {
            cancelRecognition()
        } else {
            recognitionJob = scope.launch { launchRecognition() }.apply {
                invokeOnCompletion(::jobCancellationHandler)
            }
        }
    }

    fun cancelRecognition() {
        recognitionJob?.cancel()
    }

    fun resetStatusToReady(addLastRecordToQueue: Boolean) {
        check(recognitionJob?.isCompleted == true) { "resetStatusToReady() must be invoked after recognitionJob completion" }
//        if (recognitionJob.isActive) cancelRecognition()

        when (val status = _statusFlow.value) {
            is RecognitionStatus.NoMatches -> {
                if (addLastRecordToQueue)
                    enqueuedRecognition(status.record, false)
                else
                    fileRecordRepository.delete(status.record)
            }
            is RecognitionStatus.Error.RemoteError -> {
                if (addLastRecordToQueue)
                    enqueuedRecognition(status.record, true)
                else
                    fileRecordRepository.delete(status.record)
            }
            else -> {}
        }
        _statusFlow.update { RecognitionStatus.Ready }
    }

    private suspend fun launchRecognition() {
        withContext(defaultDispatcher) {
            val userPreferences = preferencesRepository.userPreferencesFlow.first()

            _statusFlow.update { RecognitionStatus.Listening }
            val recordResult = recorderController.recordAudioToFile(
                Duration.ofMillis(RECORD_DURATION_IN_MILLIS)
            )
            Log.w("SUPER", recordResult.toString())
            when (recordResult) {
                is RecordResult.Error -> {
                    _statusFlow.update { RecognitionStatus.Error.RecordError(recordResult) }
                }
                is RecordResult.Success -> {
                    _statusFlow.update { RecognitionStatus.Recognizing }
                    val newStatus = when (val result = recognitionService.recognize(
                        token = userPreferences.apiToken,
                        requiredServices = userPreferences.requiredServices,
                        file = recordResult.file
                    )) {
                        is RemoteRecognitionResult.Success -> {
                            fileRecordRepository.delete(recordResult.file)
                            val newTrack =
                                trackRepository.insertOrReplaceSaveMetadata(result.data)[0]
                            RecognitionStatus.Success(newTrack)
                        }
                        is RemoteRecognitionResult.NoMatches -> {
                            RecognitionStatus.NoMatches(recordResult.file)
                        }
                        is RemoteRecognitionResult.Error -> {
                            RecognitionStatus.Error.RemoteError(
                                error = result,
                                record = recordResult.file
                            )
                        }
                    }
                    _statusFlow.update { newStatus }
                }
            }
        }
    }

    private fun jobCancellationHandler(cause: Throwable?) {
        Log.d("jobCompletionHandler", "cause=$cause")
        when (cause) {
            is CancellationException -> {
                recognitionJob = null
                _statusFlow.update { RecognitionStatus.Ready }
            }
        }
    }

    private fun enqueuedRecognition(recordFile: File, launchWorker: Boolean) {
        appScope.launch {
            val enqueued = EnqueuedRecognition(
                id = 0,
                title = "",
                recordFile = recordFile,
                creationDate = Instant.now()
            )
            val id = enqueuedRecognitionRepository.insertOrReplace(enqueued)
            if (launchWorker) {
                val stored = enqueued.copy(id = id)
                enqueuedRecognitionWorkManager.enqueueRecognitionWorker(stored)
            }
        }
    }

//    // FOR DEV PURPOSE
//    fun recognizeRecordedFile(file: File, scope: CoroutineScope) {
//        scope.launch {
//            recognitionJob?.cancelAndJoin()
//            recognitionJob = scope.launch {
//                withContext(defaultDispatcher) {
//                    val userPreferences = preferencesRepository.userPreferencesFlow.first()
//                    _statusFlow.update { RecognitionStatus.Recognizing }
//                    val newStatus = when (val result = recognizeService.recognize(
//                        token = userPreferences.apiToken,
//                        requiredServices = userPreferences.requiredServices,
//                        file = file
//                    )) {
//                        is RemoteRecognizeResult.Success -> {
//                            val newTrack =
//                                trackRepository.insertOrReplaceSaveMetadata(result.data)[0]
//                            RecognitionStatus.Success(newTrack)
//                        }
//                        is RemoteRecognizeResult.NoMatches -> {
//                            RecognitionStatus.NoMatches(File(""))
//                        }
//                        is RemoteRecognizeResult.Error -> {
//                            RecognitionStatus.Error.RemoteError(
//                                error = result,
//                                record = File("")
//                            )
//                        }
//                    }
//                    _statusFlow.update { newStatus }
//                }
//            }.apply {
//                invokeOnCompletion(::jobCancellationHandler)
//            }
//        }
//    }

    // FOR DEV PURPOSE
    fun fakeRecognize(scope: CoroutineScope) {
        scope.launch {
            recognitionJob?.cancelAndJoin()
            recognitionJob = scope.launch {
                withContext(defaultDispatcher) {
                    _statusFlow.update { RecognitionStatus.Listening }
                    delay(1000)
                    _statusFlow.update { RecognitionStatus.Recognizing }
                    delay(1000)
                    val newStatus = when (val result = recognitionService.fakeRecognize()) {
                        is RemoteRecognitionResult.Success -> {
                            val newTrack =
                                trackRepository.insertOrReplaceSaveMetadata(result.data)[0]
                            RecognitionStatus.Success(newTrack)
                        }
                        is RemoteRecognitionResult.NoMatches -> {
                            RecognitionStatus.NoMatches(File(""))
                        }
                        is RemoteRecognitionResult.Error -> {
                            RecognitionStatus.Error.RemoteError(
                                error = result,
                                record = File("")
                            )
                        }
                    }
                    _statusFlow.update { newStatus }
                }
            }.apply {
                invokeOnCompletion(::jobCancellationHandler)
            }
        }
    }

    //dev purpose
    fun setStatus(newStatus: RecognitionStatus) {
        _statusFlow.update { newStatus }
    }


    companion object {
        private const val RECORD_DURATION_IN_MILLIS = 6_000L
    }

}











