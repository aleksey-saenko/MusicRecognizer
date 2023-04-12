package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.remote.audd.RecognitionDataService
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import java.io.File
import javax.inject.Inject

class AdapterRecognitionService @Inject constructor(
    private val recognitionDataService: RecognitionDataService,
    private val remoteTrackResultToDomainMapper: Mapper<RemoteRecognitionDataResult<Track>, RemoteRecognitionResult<Track>>,
    private val requiredServicesToProtoMapper: Mapper<UserPreferences.RequiredServices, UserPreferencesProto.RequiredServicesProto>,
    private val trackToDomainMapper: Mapper<TrackEntity, Track>,
) : RecognitionService {

    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        file: File
    ): RemoteRecognitionResult<Track> {
        return remoteTrackResultToDomainMapper.map(
            recognitionDataService.recognize(
                token = token,
                requiredServices = requiredServicesToProtoMapper.map(requiredServices),
                file = file
            ).map { entity -> trackToDomainMapper.map(entity) }
        )
    }

}