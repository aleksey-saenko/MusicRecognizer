package com.mrsep.musicrecognizer.presentation.screens.preferences.queue

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.mrsep.musicrecognizer.di.DefaultDispatcher
import com.mrsep.musicrecognizer.domain.*
import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognitionWorkerStatus
import com.mrsep.musicrecognizer.workmanager.EnqueuedRecognitionWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ENQUEUED_FLOW_LIMIT = 50

@HiltViewModel
class QueueScreenViewModel @Inject constructor(
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository,
    private val enqueuedRecognitionWorkManager: EnqueuedRecognitionWorkManager,
    private val playerController: PlayerController,
    private val fileRecordRepository: FileRecordRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val enqueuedRecognitionUiFlow = enqueuedRecognitionRepository.getFlow(ENQUEUED_FLOW_LIMIT)
        .flatMapLatest { listEnqueuedRecognitions ->
            if (listEnqueuedRecognitions.isEmpty()) {
                flow { emit(emptyList()) }
            } else {
                val listOfFlow = listEnqueuedRecognitions.map { enqueuedRecognition ->
                    enqueuedRecognitionWorkManager.getUniqueWorkInfoFlow(enqueuedRecognition)
                        .mapLatest { workInfo ->
                            EnqueuedWithStatus(
                                enqueued = enqueuedRecognition,
                                status = EnqueuedRecognitionWorker.getWorkerStatus(workInfo)
                            )
                        }
                }
                combine(*listOfFlow.toTypedArray()) { pairs -> pairs.toList() }
//                    .combine(playerController.statusFlow) { list: List<EnqueuedWithStatus>, playerStatus: PlayerStatus ->
//                        list.map {  }
//                    }
            }
        }
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playerStatusFlow = playerController.statusFlow

    fun deleteEnqueuedRecognition(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.getById(enqueuedId)?.let { enqueued ->
                enqueuedRecognitionWorkManager.cancelRecognitionWorker(enqueued)
                enqueuedRecognitionRepository.deleteById(enqueued.id)
                fileRecordRepository.delete(enqueued.recordFile)
            }
        }
    }

    fun renameEnqueuedRecognition(enqueuedId: Int, name: String) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.getById(enqueuedId)?.let { enqueued ->
                val newEnqueued = enqueued.copy(title = name)
                enqueuedRecognitionRepository.update(newEnqueued)
            }
        }
    }

    fun startPlayRecord(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.getById(enqueuedId)?.let { enqueued ->
                playerController.start(enqueued.recordFile)
            }
        }
    }

    fun stopPlayer() {
        playerController.stop()
    }

    fun enqueueRecognition(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.getById(enqueuedId)?.let { enqueued ->
                enqueuedRecognitionWorkManager.enqueueRecognitionWorker(enqueued)
            }
        }
    }

    fun cancelRecognition(enqueuedId: Int) {
        viewModelScope.launch {
            enqueuedRecognitionRepository.getById(enqueuedId)?.let { enqueued ->
                enqueuedRecognitionWorkManager.cancelRecognitionWorker(enqueued)
            }
        }
    }

}

private fun WorkInfo?.toDebugString(): String {
    return this?.let { "id=${this.id}, state=${this.state.name}" } ?: ""
}

@Immutable
data class EnqueuedWithStatus(
    val enqueued: EnqueuedRecognition,
    val status: EnqueuedRecognitionWorkerStatus
)