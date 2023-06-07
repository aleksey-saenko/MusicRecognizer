package com.mrsep.musicrecognizer.data.remote.audd.rest

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.POST

interface AuddApi {

    @POST("/")
    suspend fun recognize(@Body multipartBody: MultipartBody): RemoteRecognitionResultDo

}