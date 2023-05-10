package com.mrsep.musicrecognizer.data.remote.audd

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.POST

interface AuddApi {

    @POST("/")
    suspend fun recognize(@Body multipartBody: MultipartBody): RemoteRecognitionDataResult

}