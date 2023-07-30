package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.RequiredServicesDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.audd.rest.RecognitionServiceDo
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamServiceDo
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences.RequiredServices
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class AdapterRecognitionService @Inject constructor(
    private val recognitionStreamService: RecognitionStreamServiceDo,
    private val recognitionService: RecognitionServiceDo,
    private val remoteResultMapper: Mapper<RemoteRecognitionResultDo, RemoteRecognitionResult>,
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesDo, RequiredServices>,
) : RemoteRecognitionService {

    override suspend fun recognize(
        token: String,
        requiredServices: RequiredServices,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResult {
        return remoteResultMapper.map(
            recognitionStreamService.recognize(
                token = token,
                requiredServices = requiredServicesMapper.reverseMap(requiredServices),
                audioRecordingFlow = audioRecordingFlow
            )
        )
    }

    override suspend fun recognize(
        token: String,
        requiredServices: RequiredServices,
        file: File
    ): RemoteRecognitionResult {
        return remoteResultMapper.map(
            recognitionService.recognize(
                token = token,
                requiredServices = requiredServicesMapper.reverseMap(requiredServices),
                file = file
            )
        )
    }

}