package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

internal interface RecognitionStatusHolder {

    val status: StateFlow<RecognitionStatus>

    val isStatusObserving: Flow<Boolean>

    fun resetFinalStatus()
}

internal interface MutableRecognitionStatusHolder : RecognitionStatusHolder {

    fun updateStatus(newStatus: RecognitionStatus)

    fun updateStatusIfObserving(newStatus: RecognitionStatus): Boolean
}

internal class RecognitionStatusHolderImpl @Inject constructor() : MutableRecognitionStatusHolder {

    private val mutableStatus = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)
    override val status = mutableStatus.asStateFlow()

    override val isStatusObserving = mutableStatus.subscriptionCount
        .map { count -> count > 0 }
        .conflate()

    override fun resetFinalStatus() {
        mutableStatus.update { oldStatus ->
            if (oldStatus is RecognitionStatus.Done) RecognitionStatus.Ready else oldStatus
        }
    }

    override fun updateStatus(newStatus: RecognitionStatus) {
        mutableStatus.update { newStatus }
    }

    override fun updateStatusIfObserving(newStatus: RecognitionStatus): Boolean {
        var isUpdated = false
        with(mutableStatus) {
            update { oldStatus ->
                isUpdated = subscriptionCount.value > 0
                if (isUpdated) newStatus else oldStatus
            }
        }
        return isUpdated
    }
}