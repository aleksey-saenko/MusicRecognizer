package com.mrsep.musicrecognizer.data.remote.audd.rest

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.TokenValidationStatusDo
import java.io.File
import java.net.URL

interface RecognitionServiceDo {

    suspend fun recognize(
        token: String,
        file: File
    ): RemoteRecognitionResultDo

    suspend fun recognize(
        token: String,
        byteArray: ByteArray
    ): RemoteRecognitionResultDo

    suspend fun recognize(
        token: String,
        url: URL
    ): RemoteRecognitionResultDo


    suspend fun validateToken(token: String): TokenValidationStatusDo

}