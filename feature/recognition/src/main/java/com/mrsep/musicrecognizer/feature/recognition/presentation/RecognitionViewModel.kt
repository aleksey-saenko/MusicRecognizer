package com.mrsep.musicrecognizer.feature.recognition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.recognition.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
internal class RecognitionViewModel @Inject constructor(
    private val recognitionInteractor: RecognitionInteractor,
    private val recorderController: RecorderController
) : ViewModel() {

    val recognizeStatusFlow = recognitionInteractor.statusFlow

    val maxAmplitudeFlow = recorderController.maxAmplitudeFlow

    fun recognizeTap() {
        recognitionInteractor.launchRecognitionOrCancel(viewModelScope)
    }

    fun resetStatusToReady(addRecordToQueue: Boolean) {
        viewModelScope.launch {
            recognitionInteractor.resetStatusToReady(addRecordToQueue)
        }
    }

}