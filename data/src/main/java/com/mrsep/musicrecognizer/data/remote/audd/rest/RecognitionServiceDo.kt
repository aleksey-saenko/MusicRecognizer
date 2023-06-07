package com.mrsep.musicrecognizer.data.remote.audd.rest

import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.TokenValidationStatusDo
import java.io.File
import java.net.URL

interface RecognitionServiceDo {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesDo.RequiredServicesDo,
        file: File
    ): RemoteRecognitionResultDo

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesDo.RequiredServicesDo,
        byteArray: ByteArray
    ): RemoteRecognitionResultDo

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesDo.RequiredServicesDo,
        url: URL
    ): RemoteRecognitionResultDo


    suspend fun validateToken(token: String): TokenValidationStatusDo

}