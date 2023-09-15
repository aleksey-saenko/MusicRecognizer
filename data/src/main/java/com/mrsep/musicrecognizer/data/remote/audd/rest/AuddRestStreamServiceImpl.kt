package com.mrsep.musicrecognizer.data.remote.audd.rest

import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamServiceDo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import javax.inject.Inject

class AuddRestStreamServiceImpl @Inject constructor(
    private val recognitionService: RecognitionServiceDo
) : RecognitionStreamServiceDo {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesDo.RequiredServicesDo,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResultDo {

        // bad recording by default in case if audioRecordingFlow is empty
        var lastResult: RemoteRecognitionResultDo = RemoteRecognitionResultDo.Error.BadRecording(
            "Recognition process failed due to empty audio recording stream"
        )
        audioRecordingFlow.flatMapMerge { recording ->
            flow {
                emit(
                    recognitionService.recognize(
                        token = token,
                        requiredServices = requiredServices,
                        byteArray = recording
                    )
                )
            }
        }
            .takeWhile { result ->
                lastResult = result
                when (result) {
                    is RemoteRecognitionResultDo.Success,
                    is RemoteRecognitionResultDo.Error.BadRecording,
                    is RemoteRecognitionResultDo.Error.WrongToken -> false

                    else -> true
                }
            }
            .collect()

        return lastResult
    }

}