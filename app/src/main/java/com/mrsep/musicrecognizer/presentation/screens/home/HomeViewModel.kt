package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.*
import com.mrsep.musicrecognizer.domain.model.RecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recognizeInteractor: RecognizeInteractor,
    private val recorderController: RecorderController,
    private val playerController: PlayerController
) : ViewModel() {

    val uiState = recognizeInteractor.statusFlow.map {
        when (it) {
            is RecognizeStatus.Ready -> MainUiState.Ready
            is RecognizeStatus.Listening -> MainUiState.Listening
            is RecognizeStatus.Recognizing -> MainUiState.Recognizing
            is RecognizeStatus.Success -> MainUiState.Success(it.result)
            is RecognizeStatus.Failure -> MainUiState.Failure(it.toString())
        }
    }

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

    //    fun recognizeTap() = recognizeInteractor.recognize(viewModelScope)
    fun recognizeTap() = recognizeInteractor.launchRecognizeOrCancel(viewModelScope)

}

sealed interface MainUiState {

    fun buttonTitle(uiHandler: MainUiHandler): String

    object Ready : MainUiState {
        override fun buttonTitle(uiHandler: MainUiHandler) = uiHandler.readyTitle
    }

    object Listening : MainUiState {
        override fun buttonTitle(uiHandler: MainUiHandler) = uiHandler.listeningTitle
    }

    object Recognizing : MainUiState {
        override fun buttonTitle(uiHandler: MainUiHandler) = uiHandler.recognizingTitle
    }

    data class Success(val result: RecognizeResult<Track>) : MainUiState {
        override fun buttonTitle(uiHandler: MainUiHandler) = uiHandler.successTitle
    }

    data class Failure(val message: String) : MainUiState {
        override fun buttonTitle(uiHandler: MainUiHandler) = uiHandler.readyTitle
    }

}

interface MainUiHandler {
    val readyTitle: String
    val listeningTitle: String
    val recognizingTitle: String
    val successTitle: String
}

//class MainUiHandlerImpl(private val context: Context) : MainUiHandler {
//    override val readyTitle get() = context.getString(R.string.tap_to_recognize)
//    override val listeningTitle get() = context.getString(R.string.listening)
//    override val recognizingTitle get() = context.getString(R.string.recognizing)
//    override val successTitle get() = context.getString(R.string.tap_to_new_recognize)
//}