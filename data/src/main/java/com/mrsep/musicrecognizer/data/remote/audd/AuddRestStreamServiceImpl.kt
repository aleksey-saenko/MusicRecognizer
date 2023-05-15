package com.mrsep.musicrecognizer.data.remote.audd

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamDataService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import javax.inject.Inject

class AuddRestStreamServiceImpl @Inject constructor(
    private val recognitionService: RecognitionDataService
) : RecognitionStreamDataService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionDataResult {

        // bad recording by default in case if audioRecordingFlow is empty
        var lastResult: RemoteRecognitionDataResult =
            RemoteRecognitionDataResult.Error.BadRecording(
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
                    is RemoteRecognitionDataResult.Success,
                    is RemoteRecognitionDataResult.Error.BadRecording,
                    is RemoteRecognitionDataResult.Error.WrongToken -> false

                    else -> true
                }
            }
            .collect()

        return lastResult
    }

}