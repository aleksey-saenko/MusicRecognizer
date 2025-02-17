package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.audio.audioplayer.PlayerController
import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import com.mrsep.musicrecognizer.core.common.util.AppDateTimeFormatter
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognitionWithStatus
import com.mrsep.musicrecognizer.core.domain.recognition.model.ScheduledJobStatus
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.EnqueuedRecognitionUi
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.PlayerStatusUi
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class QueueScreenViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val recognitionScheduler: EnqueuedRecognitionScheduler,
    private val playerController: PlayerController,
    private val dateFormatter: AppDateTimeFormatter,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val screenUiStateFlow = combine(
        flow = combine(
            enqueuedRecognitionRepository.getAllRecognitionsFlow(),
            recognitionScheduler.getJobStatusForAllFlow()
        ) { enqueuedList, statusMap ->
            enqueuedList.map { enqueued ->
                EnqueuedRecognitionWithStatus(
                    enqueued = enqueued,
                    status = statusMap[enqueued.id] ?: ScheduledJobStatus.INACTIVE
                )
            }
        },
        flow2 = playerController.statusFlow,
        flow3 = preferencesRepository.userPreferencesFlow
    ) { enqueuedWithStatusList, playerStatus, preferences ->
        QueueScreenUiState.Success(
            recognitionList = enqueuedWithStatusList
                .map { it.toUi(dateFormatter) }
                .toImmutableList(),
            playerStatus = playerStatus.toUi(),
            useGridLayout = preferences.useGridForRecognitionQueue,
            showCreationDate = preferences.showCreationDateInQueue,
        )
    }.flowOn(defaultDispatcher).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QueueScreenUiState.Loading
    )

    fun renameRecognition(recognitionId: Int, newTitle: String) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.updateTitle(recognitionId, newTitle)
        }
    }

    fun enqueueRecognition(recognitionId: Int, forceLaunch: Boolean) {
        recognitionScheduler.enqueue(listOf(recognitionId), forceLaunch = forceLaunch)
    }

    fun cancelRecognition(recognitionId: Int) {
        cancelRecognitions(listOf(recognitionId))
    }

    fun cancelRecognitions(recognitionIds: List<Int>) {
        recognitionScheduler.cancel(recognitionIds)
    }

    fun cancelAndDeleteRecognition(recognitionId: Int) {
        cancelAndDeleteRecognitions(listOf(recognitionId))
    }

    fun cancelAndDeleteRecognitions(recognitionIds: List<Int>) {
        viewModelScope.launch {
            playerController.stop()
            recognitionScheduler.cancel(recognitionIds)
            enqueuedRecognitionRepository.delete(recognitionIds)
        }
    }

    fun startAudioPlayer(recognitionId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.getRecordingForRecognition(recognitionId)?.let { recording ->
                playerController.start(recognitionId, recording)
            }
        }
    }

    fun stopAudioPlayer() {
        playerController.stop()
    }

    fun setUseGridLayout(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUseGridForRecognitionQueue(value)
        }
    }

    fun setShowCreationDate(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShowCreationDateInQueue(value)
        }
    }
}

@Immutable
internal sealed class QueueScreenUiState {

    data object Loading : QueueScreenUiState()

    data class Success(
        val recognitionList: ImmutableList<EnqueuedRecognitionUi>,
        val playerStatus: PlayerStatusUi,
        val useGridLayout: Boolean,
        val showCreationDate: Boolean,
    ) : QueueScreenUiState()
}
