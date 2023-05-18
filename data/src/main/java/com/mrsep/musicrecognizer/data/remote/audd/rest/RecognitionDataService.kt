package com.mrsep.musicrecognizer.data.remote.audd.rest

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.remote.audd.model.TokenValidationDataStatus
import java.io.File
import java.net.URL

interface RecognitionDataService {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        file: File
    ): RemoteRecognitionDataResult

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        byteArray: ByteArray
    ): RemoteRecognitionDataResult

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        url: URL
    ): RemoteRecognitionDataResult


    suspend fun validateToken(token: String): TokenValidationDataStatus

}