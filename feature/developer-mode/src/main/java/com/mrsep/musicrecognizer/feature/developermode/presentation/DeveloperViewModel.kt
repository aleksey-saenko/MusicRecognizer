package com.mrsep.musicrecognizer.feature.developermode.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSourceDo
import com.mrsep.musicrecognizer.data.audiorecord.encoder.AacEncoder
import com.mrsep.musicrecognizer.data.player.MediaPlayerController
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.remote.audd.rest.RecognitionServiceDo
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.util.DatabaseFiller
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
internal class DeveloperViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val databaseFiller: DatabaseFiller,
    private val trackRepositoryDo: TrackRepositoryDo,
    private val recognitionServiceDo: RecognitionServiceDo,
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

    fun prepopulateDbFakes() = processSuspend { databaseFiller.prepopulateByFaker(count = 10_000) }

    fun prepopulateDbAssets() = processSuspend { databaseFiller.prepopulateFromAssets() }


    fun recognizeTestFile() {
        viewModelScope.launch {
            val result = recognitionServiceDo.recognize(
                token = "",
                requiredServices = UserPreferencesDo.RequiredServicesDo(
                    spotify = true,
                    youtube = false,
                    soundCloud = false,
                    appleMusic = false,
                    deezer = false,
                    napster = false,
                    musicbrainz = false
                ),
                file = File("${appContext.filesDir.absolutePath}/testAudioChain")
            )
            println(result)
        }
    }

    val amplitudeFlow get() = amplitudeSource.amplitudeFlow
    private var _audioChainJob: Job? = null
    private val destination = ByteArrayOutputStream()

    fun testAudioChain() {
        _audioChainJob = viewModelScope.launch(Dispatchers.IO) {
            encoder.aacPacketsFlow.takeWhile { it.isSuccess }.collect {
                destination.write(it.getOrNull()!!.data)
            }
        }
    }

    fun writeChainResult() {
        viewModelScope.launch(Dispatchers.IO) {
            appContext.writeToFile(destination.toByteArray(), "testAudioChain")
        }
    }

    fun playChainResult() {
        playerController.start(File("${appContext.filesDir.absolutePath}/testAudioChain"))
    }

    fun stopTestAudioChain() = _audioChainJob?.cancel()



}

private fun Context.writeToFile(byteArray: ByteArray, filename: String) {
    println("write to file, size=${byteArray.size}")
    val outputFile = File("${filesDir.absolutePath}/$filename")
    outputFile.createNewFile()
    outputFile.outputStream().buffered().use { it.write(byteArray) }
    println("write to file finished")
}