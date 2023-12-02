package com.mrsep.musicrecognizer.data.remote.audd.rest

import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.TokenValidationStatusDo
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await
import java.io.File
import java.io.IOException
import java.net.URL
import javax.inject.Inject

private const val mediaTypeString = "audio/mpeg; charset=utf-8"
private const val AUDIO_SAMPLE_URL = "https://audd.tech/example.mp3"

private const val AUDD_REST_BASE_URL = "https://api.audd.io/"
private const val AUDD_RETURN_PARAM = "lyrics,spotify,apple_music,deezer,napster,musicbrainz"

class AuddRecognitionServicePure @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
    moshi: Moshi
) : RecognitionServiceDo {

    @OptIn(ExperimentalStdlibApi::class)
    private val moshiAdapter = moshi.adapter<RemoteRecognitionResultDo>()

    override suspend fun recognize(
        token: String,
        file: File
    ): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            baseCall(
                token = token,
                dataBodyPart = { addFileAsMultipartBody(file) }
            )
        }
    }

    override suspend fun recognize(
        token: String,
        byteArray: ByteArray
    ): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            baseCall(
                token = token,
                dataBodyPart = { addByteArrayAsMultipartBody(byteArray) }
            )
        }
    }

    override suspend fun recognize(
        token: String,
        url: URL
    ): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            baseCall(
                token = token,
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

    private suspend fun baseCall(
        token: String,
        returnParam: String = AUDD_RETURN_PARAM,
        dataBodyPart: MultipartBody.Builder.() -> MultipartBody.Builder
    ): RemoteRecognitionResultDo {
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_token", token)
            .addFormDataPart("return", returnParam)
            .dataBodyPart()
            .build()
        val request = Request.Builder()
            .url(AUDD_REST_BASE_URL)
            .post(multipartBody)
            .build()
        return try {
            val response = okHttpClient.newCall(request).await()
            if (response.isSuccessful) {
                try {
                    moshiAdapter.fromJson(response.body!!.source())!!
                } catch (e: Exception) {
                    e.printStackTrace()
                    RemoteRecognitionResultDo.Error.UnhandledError(message = e.message ?: "", e = e)
                }
            } else {
                RemoteRecognitionResultDo.Error.HttpError(
                    code = response.code,
                    message = response.message
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            RemoteRecognitionResultDo.Error.BadConnection
        } catch (e: Exception) {
            e.printStackTrace()
            RemoteRecognitionResultDo.Error.UnhandledError(message = e.message ?: "", e = e)
        }
    }

    override suspend fun validateToken(token: String): TokenValidationStatusDo {
        if (token.isBlank()) return TokenValidationStatusDo.Error(
            RemoteRecognitionResultDo.Error.WrongToken(false)
        )
        val sampleUrlRecognitionResult = withContext(ioDispatcher) {
            baseCall(
                token = token,
                returnParam = "",
                dataBodyPart = { addUrlAsMultipartBody(URL(AUDIO_SAMPLE_URL)) }
            )
        }
        return when (sampleUrlRecognitionResult) {
            is RemoteRecognitionResultDo.Error ->
                TokenValidationStatusDo.Error(sampleUrlRecognitionResult)
            RemoteRecognitionResultDo.NoMatches,
            is RemoteRecognitionResultDo.Success -> TokenValidationStatusDo.Success
        }
    }

}