package com.mrsep.musicrecognizer.feature.recognitionqueue.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ENQUEUED_FLOW_LIMIT = 50

@HiltViewModel
internal class QueueScreenViewModel @Inject constructor(
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val playerController: PlayerController
) : ViewModel() {

    val enqueuedRecognitionUiFlow = enqueuedRecognitionRepository
        .getAllFlowWithStatus(ENQUEUED_FLOW_LIMIT)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playerStatusFlow = playerController.statusFlow

    fun deleteEnqueuedRecognition(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.cancelAndDeleteById(enqueuedId)
        }
    }

    fun renameEnqueuedRecognition(enqueuedId: Int, newTitle: String) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.updateTitle(enqueuedId, newTitle)
        }
    }

    fun startPlayRecord(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.getRecordById(enqueuedId)?.let { recordFile ->
                playerController.start(recordFile)
            }
        }
    }

    fun stopPlayer() {
        playerController.stop()
    }

    fun enqueueRecognition(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.enqueueById(enqueuedId)
        }
    }

    fun cancelRecognition(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.cancelById(enqueuedId)
        }
    }

}