package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import com.mrsep.musicrecognizer.feature.recognition.domain.*
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Duration
import javax.inject.Inject

internal class RecognitionInteractorImpl @Inject constructor(
    private val recognitionService: RecognitionService,
    private val trackRepository: TrackRepository,
    private val recorderController: RecorderController,
    private val preferencesRepository: PreferencesRepository,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val fileRecordRepository: FileRecordRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : RecognitionInteractor {

    private val _statusFlow = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)
    override val statusFlow = _statusFlow.asStateFlow()

    private var recognitionJob: Job? = null

    // should be refactored:
    // you should check for recognitionJob?.isCompleted (add canceling state?)
    override fun launchRecognitionOrCancel(scope: CoroutineScope) {
        if (recognitionJob?.isActive == true) {
            cancelRecognition()
        } else {
            recognitionJob = scope.launch { launchRecognition() }.apply {
                invokeOnCompletion(::jobCancellationHandler)
            }
        }
    }

    override fun cancelRecognition() {
        recognitionJob?.cancel()
    }

    override suspend fun resetStatusToReady(addLastRecordToQueue: Boolean) {
        check(recognitionJob?.isCompleted == true) {
            "resetStatusToReady() must be invoked after recognitionJob completion"
        }

        when (val status = _statusFlow.value) {
            is RecognitionStatus.NoMatches -> {
                if (addLastRecordToQueue)
                    enqueuedRecognitionRepository.createEnqueuedRecognition(status.record, false)
                else
                    fileRecordRepository.delete(status.record)
            }
            is RecognitionStatus.Error.RemoteError -> {
                if (addLastRecordToQueue)
                    enqueuedRecognitionRepository.createEnqueuedRecognition(status.record, true)
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
        when (cause) {
            is CancellationException -> {
                recognitionJob = null
                _statusFlow.update { RecognitionStatus.Ready }
            }
        }
    }

    companion object {
        private const val RECORD_DURATION_IN_MILLIS = 6_000L
    }

}











