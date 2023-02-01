package com.mrsep.musicrecognizer.data.remote.audd

import com.mrsep.musicrecognizer.data.remote.audd.model.AuddResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.POST

interface AuddApi {

    @POST("/")
    suspend fun recognize(@Body multipartBody: MultipartBody): AuddResponse

    @POST("/recognizeWithOffset")
    suspend fun recognizeHumming(@Body multipartBody: MultipartBody): String

}