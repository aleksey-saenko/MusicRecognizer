package com.mrsep.musicrecognizer.feature.developermode.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSource
import com.mrsep.musicrecognizer.data.audiorecord.encoder.AacEncoder
import com.mrsep.musicrecognizer.data.audiorecord.encoder.AacEncoderController
import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.data.player.MediaPlayerController
import com.mrsep.musicrecognizer.data.remote.audd.RecognitionDataService
import com.mrsep.musicrecognizer.data.track.TrackDataRepository
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
internal class DeveloperViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val databaseFiller: DatabaseFiller,
    private val trackDataRepository: TrackDataRepository,
    private val recognitionDataService: RecognitionDataService,
    private val audioSource: SoundSource,
    private val amplitudeSource: SoundAmplitudeSource,
    private val encoder: AacEncoder,
//    private val auddWsService: AuddRecognitionWsService,
//    private val auddScarletApi: AuddScarletApi,
    private val aacEncoderController: AacEncoderController,
    private val playerController: MediaPlayerController
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

    fun clearDb() = processSuspend { trackDataRepository.deleteAll() }

    fun prepopulateDbFakes() = processSuspend { databaseFiller.prepopulateByFaker(1000) }

    fun prepopulateDbAssets() = processSuspend { databaseFiller.prepopulateFromAssets() }


    fun recognizeTestFile() {
        viewModelScope.launch {
            val result = recognitionDataService.recognize(
                token = "",
                requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
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

    //OKHTTP WEB SOCKET
    private var _webSocketJob: Job? = null
    fun startWebSocketConnection() {
        val token = com.mrsep.musicrecognizer.data.BuildConfig.AUDD_TOKEN
        val services = UserPreferencesProto.RequiredServicesProto.getDefaultInstance()
        TODO("not implemented")
//        _webSocketJob = viewModelScope.launch {
////            auddWsService.startSession(token, services, recordingFlow, strategy.size).collect {
////                println(it)
////            }
//        }
    }
    fun stopWebSocketConnection() = _webSocketJob?.cancel()

    //SCARLET WEB SOCKET
    private var _scarletJob: Job? = null
    fun startScarletTest() {
        val token = com.mrsep.musicrecognizer.data.BuildConfig.AUDD_TOKEN
        val services = UserPreferencesProto.RequiredServicesProto.getDefaultInstance()
        TODO("not implemented")
//        _scarletJob = viewModelScope.launch {
//            auddScarletApi.observeEvents().collect { webSocketEvent ->
//                println("webSocketEvent=${webSocketEvent}")
//            }
//        }
    }
    fun stopScarletTest() = _scarletJob?.cancel()


}

private fun Context.writeToFile(byteArray: ByteArray, filename: String) {
    println("write to file, size=${byteArray.size}")
    val outputFile = File("${filesDir.absolutePath}/$filename")
    outputFile.createNewFile()
    outputFile.outputStream().buffered().use { it.write(byteArray) }
    println("write to file finished")
}