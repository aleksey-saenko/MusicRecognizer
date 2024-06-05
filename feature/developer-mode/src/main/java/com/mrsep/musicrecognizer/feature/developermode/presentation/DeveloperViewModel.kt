package com.mrsep.musicrecognizer.feature.developermode.presentation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.data.audioplayer.MediaPlayerController
import com.mrsep.musicrecognizer.data.audioplayer.PlayerStatusDo
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSourceDo
import com.mrsep.musicrecognizer.data.audiorecord.encoder.AacEncoder
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.util.DatabaseFiller
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class DeveloperViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val databaseFiller: DatabaseFiller,
//    private val recognitionDatabaseFiller: EnqRecognitionDBFiller,
    private val trackRepositoryDo: TrackRepositoryDo,
    private val amplitudeSource: SoundAmplitudeSourceDo,
    private val encoder: AacEncoder,
    private val playerController: MediaPlayerController,
) : ViewModel() {

    private var counter = MutableStateFlow(0)

    val isProcessing = counter.map { it > 0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private fun processSuspend(block: suspend () -> Unit) {
        viewModelScope.launch {
            counter.getAndUpdate { value -> value + 1 }
            block()
            counter.getAndUpdate { value -> value - 1 }
        }
    }

    fun clearDb() = processSuspend { trackRepositoryDo.deleteAll() }

    fun prepopulateDbFakes() = processSuspend { databaseFiller.prepopulateByFaker(count = 1_000) }

    fun prepopulateDbAssets() = processSuspend { databaseFiller.prepopulateFromAssets() }

    fun prepopulateRecognitionDb() { }
//        processSuspend { recognitionDatabaseFiller.prepopulateWithStoredTracks() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val amplitudeFlow
        get() = isRecording.flatMapLatest { rec ->
            if (rec) amplitudeSource.amplitudeFlow else flowOf(0f)
        }

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    val isPlaying = playerController.statusFlow
        .map { it is PlayerStatusDo.Started }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    private var _audioChainJob: Job? = null
    private val destination = ByteArrayOutputStream()

    fun startAudioRecord() {
        _isRecording.update { true }
        destination.reset()
        playerController.stop()
        _audioChainJob = viewModelScope.launch(Dispatchers.IO) {
            encoder.aacPacketsFlow.takeWhile { it.isSuccess }.collect {
                destination.write(it.getOrNull()!!.data)
            }
        }
    }

    fun stopAudioRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            _audioChainJob?.cancelAndJoin()
            val data = destination.toByteArray()
            appContext.writeToFile(data, "testAudio")
            _isRecording.update { false }
        }
    }

    fun startPlayer() {
        if (_audioChainJob == null || _audioChainJob?.isCompleted == true) {
            playerController.start(id = 0, file = File("${appContext.filesDir.absolutePath}/testAudio"))
        }
    }

    fun stopPlayer() {
        playerController.stop()
    }
}

private fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

private fun Context.writeToFile(byteArray: ByteArray, filename: String) {
    println("write to file, size=${byteArray.size}")
    val outputFile = File("${filesDir.absolutePath}/$filename")
    outputFile.createNewFile()
    outputFile.outputStream().buffered().use { it.write(byteArray) }
    println("write to file finished")
}
