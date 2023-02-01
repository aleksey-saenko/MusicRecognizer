package com.mrsep.musicrecognizer.data.remote.audd

import android.content.Context
import com.mrsep.musicrecognizer.data.recorder.MediaRecorderController
import com.mrsep.musicrecognizer.data.remote.audd.model.AuddResponse
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.create
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuddRemoteDataSource @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val moshi: Moshi,
    retrofit: Retrofit,
) {
    private val auddClient = retrofit.create<AuddApi>()

    private val token = "your_api_key"
    private val returnParam =
        "lyrics,apple_music,spotify,deezer,napster,musicbrainz" //lyrics,apple_music,spotify,deezer,napster,musicbrainz
    private val mediaTypeString = "audio/mpeg; charset=utf-8"

    suspend fun recognize(recordFile: File = MediaRecorderController.getRecordFile(appContext)): AuddResponse {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            auddClient.recognize(
                MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("api_token", token)
                    .addFormDataPart("return", returnParam)
                    .addFormDataPart(
                        "file",
                        recordFile.name,
                        recordFile.asRequestBody(mediaTypeString.toMediaTypeOrNull())
                    )
                    .build()
            )
        }
    }

    suspend fun fakeRecognize(): AuddResponse {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            delay(1_000)
            val fakeJson = appContext.assets.open("fake_json.txt").bufferedReader().use {
                it.readText()
            }
            moshi.adapter(AuddResponse::class.java).fromJson(fakeJson)!!
        }
    }

}