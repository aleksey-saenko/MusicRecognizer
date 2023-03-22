package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.data.recorder.AudioRecorderControllerNew
import com.mrsep.musicrecognizer.di.DefaultDispatcher
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.*
import com.mrsep.musicrecognizer.util.DatabaseFiller
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recognizeInteractor: RecognizeInteractor,
    private val recorderController: RecorderController,
    private val playerController: PlayerController,
    private val trackRepository: TrackRepository,
    preferencesRepository: PreferencesRepository,
    private val databaseFiller: DatabaseFiller,
    private val audioRecorderController: AudioRecorderControllerNew,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    val preferencesFlow = preferencesRepository.userPreferencesFlow
    val recognizeStatusFlow = recognizeInteractor.statusFlow

    val ampFlow get() = recognizeInteractor.maxAmplitudeFlow

    private var recordJob: Job? = null

    fun startRecordMR() {
        recordJob = viewModelScope.launch(ioDispatcher) {//
            recorderController.recordAudioToFile(
                recognizeInteractor.recordFile,
                Duration.ofMillis(100_000)
            )
        }
    }

    fun stopRecordMR() = recordJob?.cancel()

    fun startRecordAR() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                audioRecorderController.recordAudioToFile("ar_record.raw", Duration.ofSeconds(12L))
            }
        }
    }

    fun stopRecordAR() {

    }

    fun recognize() =
        viewModelScope.launch { recognizeInteractor.recognizeRecordedFile(viewModelScope) }

    fun fakeRecognize() =
        viewModelScope.launch { recognizeInteractor.fakeRecognize(viewModelScope) }

    fun startPlayAudio() = playerController.startPlay(recognizeInteractor.recordFile)
    fun stopPlayAudio() = playerController.stopPlay()

    fun prepopulateDatabase() {
        viewModelScope.launch(defaultDispatcher) {
            delay(2000)
            databaseFiller.prepopulateDatabaseFromAssets()
        }
    }

    fun clearDatabase() = viewModelScope.launch { trackRepository.deleteAll() }

    fun recognizeTap() = recognizeInteractor.launchRecognizeOrCancel(viewModelScope)
//    fun recognizeTap() {
//        viewModelScope.launch {
//            recognizeInteractor.setStatus(RecognizeStatus.Listening)
//            delay(1000)
//            recognizeInteractor.setStatus(RecognizeStatus.Recognizing)
//            delay(1000)
////            recognizeInteractor.setStatus(RecognizeStatus.Error.RecordError(RecordResult.Error.PrepareFailed))
////            recognizeInteractor.setStatus(RecognizeStatus.Error.RemoteError(RemoteRecognizeResult.Error.BadConnection))
////            recognizeInteractor.setStatus(RecognizeStatus.Error.RemoteError(RemoteRecognizeResult.Error.WrongToken(true)))
////            recognizeInteractor.setStatus(RecognizeStatus.Error.RemoteError(RemoteRecognizeResult.Error.UnhandledError(e = RuntimeException("test"))))
//            recognizeInteractor.setStatus(RecognizeStatus.Error.RemoteError(RemoteRecognizeResult.Error.HttpError(400, "Server down")))
//        }
//    }

    fun resetStatusToReady() = recognizeInteractor.resetStatusToReady()

}