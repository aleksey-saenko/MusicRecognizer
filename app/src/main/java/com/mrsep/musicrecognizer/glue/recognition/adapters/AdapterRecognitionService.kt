package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamDataService
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AdapterRecognitionService @Inject constructor(
    private val recognitionDataService: RecognitionStreamDataService,
    private val remoteResultMapper: Mapper<RemoteRecognitionDataResult, RemoteRecognitionResult>,
    private val requiredServicesMapper: Mapper<UserPreferences.RequiredServices, UserPreferencesProto.RequiredServicesProto>,
) : RemoteRecognitionService {

    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResult {
        return remoteResultMapper.map(
            recognitionDataService.recognize(
                token = token,
                requiredServices = requiredServicesMapper.map(requiredServices),
                audioRecordingFlow = audioRecordingFlow
            )
        )
    }


}