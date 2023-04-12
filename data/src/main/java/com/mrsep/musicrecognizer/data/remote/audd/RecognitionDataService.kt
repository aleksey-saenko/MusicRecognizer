package com.mrsep.musicrecognizer.data.remote.audd

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.track.TrackEntity
import java.io.File
import java.net.URL

interface RecognitionDataService {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        file: File
    ): RemoteRecognitionDataResult<TrackEntity>

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        byteArray: ByteArray
    ): RemoteRecognitionDataResult<TrackEntity>

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        url: URL
    ): RemoteRecognitionDataResult<TrackEntity>

}