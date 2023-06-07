package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.RequiredServicesDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamServiceDo
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences.RequiredServices
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AdapterRecognitionService @Inject constructor(
    private val recognitionDataService: RecognitionStreamServiceDo,
    private val remoteResultMapper: Mapper<RemoteRecognitionResultDo, RemoteRecognitionResult>,
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesDo, RequiredServices>,
) : RemoteRecognitionService {

    override suspend fun recognize(
        token: String,
        requiredServices: RequiredServices,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResult {
        return remoteResultMapper.map(
            recognitionDataService.recognize(
                token = token,
                requiredServices = requiredServicesMapper.reverseMap(requiredServices),
                audioRecordingFlow = audioRecordingFlow
            )
        )
    }


}