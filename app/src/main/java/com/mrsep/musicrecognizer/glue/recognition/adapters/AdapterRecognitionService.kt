package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.audd.rest.RecognitionServiceDo
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamServiceDo
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class AdapterRecognitionService @Inject constructor(
    private val recognitionStreamService: RecognitionStreamServiceDo,
    private val recognitionService: RecognitionServiceDo,
    private val remoteResultMapper: Mapper<RemoteRecognitionResultDo, RemoteRecognitionResult>,
) : RemoteRecognitionService {

    override suspend fun recognize(
        token: String,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResult {
        return remoteResultMapper.map(
            recognitionStreamService.recognize(
                token = token,
                audioRecordingFlow = audioRecordingFlow
            )
        )
    }

    override suspend fun recognize(
        token: String,
        file: File
    ): RemoteRecognitionResult {
        return remoteResultMapper.map(
            recognitionService.recognize(
                token = token,
                file = file
            )
        )
    }

}