package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recognizeInteractor: RecognizeInteractor,
    private val recorderController: RecorderController,
    private val playerController: PlayerController
) : ViewModel() {

    val recognizeStatusFlow = recognizeInteractor.statusFlow

    private var recordJob: Job? = null

    fun startRecordMR() {
        recordJob = viewModelScope.launch {
            recorderController.recordAudioToFile(
                recognizeInteractor.recordFile,
                Duration.ofMillis(100_000)
            )
        }
    }

    fun stopRecordMR() = recordJob?.cancel()

    fun recognize() =
        viewModelScope.launch { recognizeInteractor.recognizeRecordedFile(viewModelScope) }

    fun fakeRecognize() =
        viewModelScope.launch { recognizeInteractor.fakeRecognize(viewModelScope) }

    fun startPlayAudio() = playerController.startPlay(recognizeInteractor.recordFile)
    fun stopPlayAudio() = playerController.stopPlay()

    fun recognizeTap() = recognizeInteractor.launchRecognizeOrCancel(viewModelScope)

    fun resetRecognizer() = recognizeInteractor.resetRecognizer()

}