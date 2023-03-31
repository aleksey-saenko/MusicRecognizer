package com.mrsep.musicrecognizer.data.remote.audd

import android.content.Context
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.RecognitionService
import com.mrsep.musicrecognizer.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.create
import java.io.File
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

private const val mediaTypeString = "audio/mpeg; charset=utf-8"

@Singleton
class AuddRecognitionService @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val moshi: Moshi,
    retrofit: Retrofit,
) : RecognitionService {
    private val auddClient = retrofit.create<AuddApi>()

    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        file: File
    ): RemoteRecognitionResult<Track> {
        return withContext(ioDispatcher) {
            baseCallFunction(
                token = token,
                requiredServices = requiredServices,
                dataBodyPart = { addFileAsMultipartBody(file) }
            )
        }
    }

    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        byteArray: ByteArray
    ): RemoteRecognitionResult<Track> {
        return withContext(ioDispatcher) {
            baseCallFunction(
                token = token,
                requiredServices = requiredServices,
                dataBodyPart = { addByteArrayAsMultipartBody(byteArray) }
            )
        }
    }

    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        url: URL
    ): RemoteRecognitionResult<Track> {
        return withContext(ioDispatcher) {
            baseCallFunction(
                token = token,
                requiredServices = requiredServices,
                dataBodyPart = { addUrlAsMultipartBody(url) }
            )
        }
    }

    private fun MultipartBody.Builder.addUrlAsMultipartBody(url: URL): MultipartBody.Builder {
        return this.addFormDataPart("url", url.toExternalForm())
    }

    private fun MultipartBody.Builder.addFileAsMultipartBody(
        file: File
    ): MultipartBody.Builder {
        return this.addFormDataPart(
            "file",
            file.name,
            file.asRequestBody(mediaTypeString.toMediaTypeOrNull())
        )
    }

    private fun MultipartBody.Builder.addByteArrayAsMultipartBody(
        byteArray: ByteArray
    ): MultipartBody.Builder {
        return this.addFormDataPart(
            "file",
            "byteArray",
            byteArray.toRequestBody(mediaTypeString.toMediaTypeOrNull())
        )
    }

    private suspend fun baseCallFunction(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        dataBodyPart: MultipartBody.Builder.() -> MultipartBody.Builder
    ): RemoteRecognitionResult<Track> {
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("api_token", token)
            .addFormDataPart("return", requiredServices.toAuddReturnParameter())
            .dataBodyPart()
            .build()
        return try {
            auddClient.recognize(multipartBody)
        } catch (e: HttpException) {
            e.printStackTrace()
            RemoteRecognitionResult.Error.HttpError(code = e.code(), message = e.message())
        } catch (e: IOException) {
            e.printStackTrace()
            RemoteRecognitionResult.Error.BadConnection
        } catch (e: Exception) {
            e.printStackTrace()
            RemoteRecognitionResult.Error.UnhandledError(message = e.message ?: "", e = e)
        }
    }


    override suspend fun fakeRecognize(): RemoteRecognitionResult<Track> {
        return withContext(ioDispatcher) {
            delay(1_000)
            val fakeJson = appContext.assets.open("fake_json_success.txt").bufferedReader().use {
                it.readText()
            }
            val resultType = Types.newParameterizedType(
                RemoteRecognitionResult::class.java,
                Track::class.java
            )
            moshi.adapter<RemoteRecognitionResult<Track>>(resultType).fromJson(fakeJson)!!
        }
    }

}


private fun UserPreferences.RequiredServices.toAuddReturnParameter(): String {
    return "lyrics"
        .plusIf(this.spotify, "spotify")
        .plusIf(this.appleMusic, "apple_music")
        .plusIf(this.deezer, "deezer")
        .plusIf(this.napster, "napster")
        .plusIf(this.musicbrainz, "musicbrainz")
}

private fun String.plusIf(conditional: Boolean, adjunct: String, separator: String = ",") =
    if (conditional) "$this$separator$adjunct" else this