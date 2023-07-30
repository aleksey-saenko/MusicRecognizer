package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

internal class RecognitionResultDelegator @Inject constructor() {

    private val _screenState = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)
    private val _serviceState = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)

    val screenState = _screenState.asStateFlow()
    val serviceState = _serviceState.asStateFlow()

    fun notify(status: RecognitionStatus) {
        when (status) {
            RecognitionStatus.Ready,
            is RecognitionStatus.Recognizing -> {
                _screenState.update { status }
                _serviceState.update { status }
            }
            is RecognitionStatus.Done -> {
                _screenState.update { status }
                if (_screenState.isNotObserved()) {
                    _serviceState.updateIfObserved(status)
                } else {
                    _serviceState.update { RecognitionStatus.Ready }
                }
            }
        }
    }

    private fun MutableStateFlow<*>.isNotObserved() = subscriptionCount.value == 0

    private fun MutableStateFlow<RecognitionStatus>.updateIfObserved(status: RecognitionStatus): Boolean {
        return if (isNotObserved()) {
            false
        } else {
            update { status }
            true
        }
    }

}