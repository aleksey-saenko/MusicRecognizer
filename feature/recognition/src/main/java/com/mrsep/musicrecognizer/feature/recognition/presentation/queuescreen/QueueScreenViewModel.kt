package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.PlayerController
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognitionWithStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.PlayerStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduledJobStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class QueueScreenViewModel @Inject constructor(
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val recognitionScheduler: EnqueuedRecognitionScheduler,
    private val playerController: PlayerController,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val screenUiStateFlow = combine(
        flow = combine(
            enqueuedRecognitionRepository.getFlowAll(),
            recognitionScheduler.getStatusFlowAll()
        ) { enqueuedList, statusMap ->
            enqueuedList.map { enqueued ->
                EnqueuedRecognitionWithStatus(
                    enqueued = enqueued,
                    status = statusMap[enqueued.id] ?: ScheduledJobStatus.INACTIVE
                )
            }
        },
        flow2 = playerController.statusFlow
    ) { enqueuedWithStatusList, playerStatus ->
        QueueScreenUiState.Success(
            enqueuedList = enqueuedWithStatusList.toImmutableList(),
            playerStatus = playerStatus
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QueueScreenUiState.Loading
    )

    fun renameRecognition(enqueuedId: Int, newTitle: String) {
        appScope.launch(ioDispatcher) {
            enqueuedRecognitionRepository.updateTitle(enqueuedId, newTitle)
        }
    }

    fun enqueueRecognition(enqueuedId: Int, forceLaunch: Boolean) {
        appScope.launch(ioDispatcher) {
            recognitionScheduler.enqueueById(enqueuedId, forceLaunch = forceLaunch)
        }
    }

    fun cancelRecognition(vararg enqueuedId: Int) {
        appScope.launch(ioDispatcher) {
            recognitionScheduler.cancelById(*enqueuedId)
        }
    }

    fun cancelAndDeleteRecognition(vararg enqueuedId: Int) {
        appScope.launch(ioDispatcher) {
            playerController.stop()
            recognitionScheduler.cancelById(*enqueuedId)
            enqueuedRecognitionRepository.deleteById(*enqueuedId)
        }
    }

    fun startAudioPlayer(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.getRecordingById(enqueuedId)?.let { recordFile ->
                playerController.start(recordFile)
            }
        }
    }

    fun stopAudioPlayer() {
        playerController.stop()
    }

}

@Immutable
internal sealed class QueueScreenUiState {

    data object Loading : QueueScreenUiState()

    data class Success(
        val enqueuedList: ImmutableList<EnqueuedRecognitionWithStatus>,
        val playerStatus: PlayerStatus
    ) : QueueScreenUiState()

}