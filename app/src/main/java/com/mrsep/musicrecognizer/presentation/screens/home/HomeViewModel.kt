package com.mrsep.musicrecognizer.presentation.screens.home

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.*
import com.mrsep.musicrecognizer.data.player.MediaPlayerController
import com.mrsep.musicrecognizer.data.recorder.AudioRecorderController
import com.mrsep.musicrecognizer.data.recorder.MediaRecorderController
import com.mrsep.musicrecognizer.data.remote.audd.AuddRemoteDataSource
import com.mrsep.musicrecognizer.data.remote.audd.model.AuddResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auddRemoteDataSource: AuddRemoteDataSource,
    private val mediaRecorderController: MediaRecorderController,
    private val audioRecorderController: AudioRecorderController,
    private val mediaPlayerController: MediaPlayerController
) : ViewModel(){

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Ready)
    val uiState: StateFlow<MainUiState> get() = _uiState

    private var recognizeJob: Job? = null

    fun recognize() = viewModelScope.launch {
        _uiState.update { MainUiState.Success(auddRemoteDataSource.recognize()) }
    }

    fun fakeRecognize() = viewModelScope.launch {
        _uiState.update { MainUiState.Success(auddRemoteDataSource.fakeRecognize()) }
    }

    fun startRecordMR() = mediaRecorderController.startRecord()
    fun stopRecordMR() = mediaRecorderController.stopRecord()

    fun startPlayAudio() = mediaPlayerController.playAudio()
    fun stopPlayAudio() = mediaPlayerController.stopPlay()

    //    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @SuppressLint("MissingPermission")
    fun startRecordAR() = audioRecorderController.simpleMicRecording()

    fun recognizeTap() {
        if (recognizeJob != null) {
            recognizeJob?.cancel()
            stopRecordMR()
            recognizeJob = null
            _uiState.update { MainUiState.Ready }
        } else {
            recognizeJob = viewModelScope.launch {
                _uiState.update { MainUiState.Listening }
                startRecordMR()
                delay(12_000)
                stopRecordMR()
                _uiState.update { MainUiState.Recognizing }
                recognize()
            }
        }
    }

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

    data class Success(val data: AuddResponse) : MainUiState {
        override fun buttonTitle(uiHandler: MainUiHandler) = uiHandler.successTitle
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