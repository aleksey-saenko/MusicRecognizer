package com.mrsep.musicrecognizer.feature.recognitionqueue.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.PlayerController
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class QueueScreenViewModel @Inject constructor(
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val playerController: PlayerController
) : ViewModel() {

    val screenUiStateFlow = enqueuedRecognitionRepository
        .getAllFlowWithStatus()
        .combine(playerController.statusFlow) { enqueuedList, playerStatus ->
            QueueScreenUiState.Success(
                enqueuedList = enqueuedList.toImmutableList(),
                playerStatus = playerStatus
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QueueScreenUiState.Loading
        )

    fun enqueueRecognition(vararg enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.enqueueById(*enqueuedId)
        }
    }

    fun cancelRecognition(vararg enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.cancelById(*enqueuedId)
        }
    }

    fun cancelAndDeleteRecognition(vararg enqueuedId: Int) {
        viewModelScope.launch {
            stopAudioPlayer()
            enqueuedRecognitionRepository.cancelAndDeleteById(*enqueuedId)
        }
    }

    fun renameRecognition(enqueuedId: Int, newTitle: String) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.updateTitle(enqueuedId, newTitle)
        }
    }

    fun cancelAndDeleteRecognitionAll() {
        viewModelScope.launch {
            stopAudioPlayer()
            enqueuedRecognitionRepository.cancelAndDeleteAll()
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
sealed class QueueScreenUiState {

    object Loading : QueueScreenUiState()

    data class Success(
        val enqueuedList: ImmutableList<EnqueuedRecognition>,
        val playerStatus: PlayerStatus
    ) : QueueScreenUiState()

}