package com.mrsep.musicrecognizer.presentation.screens.workshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.data.recorder.AudioRecorderControllerFlow
import com.mrsep.musicrecognizer.data.remote.audd.AuddRecognitionWsService
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WorkshopViewModel @Inject constructor(
    private val auddRecognitionWsService: AuddRecognitionWsService,
    private val audioRecorderControllerFlow: AudioRecorderControllerFlow
) : ViewModel() {

    fun startWebSocketTest() {
        val token = "89707f838d26e4dcb9b48d7fa6fbc534"
        val services = UserPreferences.RequiredServices(
            spotify = false,
            appleMusic = true,
            deezer = false,
            napster = false,
            musicbrainz = false
        )
        viewModelScope.launch(Dispatchers.IO) {
            auddRecognitionWsService.startSession(token, services)
        }
    }

    val recordFlow get() = audioRecorderControllerFlow.recordFlow
    val calculatedFlow get() = audioRecorderControllerFlow.calculatedFlow(12)

    fun startFlowTest() {
        audioRecorderControllerFlow.test()
//        viewModelScope.launch {
//            val job = launch {
//                calculatedFlow.collect {
////                    Log.d("Flow Test", "capacity=${it.capacity()}, remaining=${it.remaining()} DATA:$it")
////                    Log.d("Flow Test", "floatArraySize=${it.size}, max:${it.maxOrNull()}, min:${it.minOrNull()}")
//                    Log.d("Flow Test", "floatArraySize=${it.size}, seconds=${it.size / 44100f}")
//                }
//            }
//            Log.d("Flow Test", "start waiting")
//            delay(16000)
//            job.cancel()
//            Log.d("Flow Test", "job canceled")
//
//        }
    }


}