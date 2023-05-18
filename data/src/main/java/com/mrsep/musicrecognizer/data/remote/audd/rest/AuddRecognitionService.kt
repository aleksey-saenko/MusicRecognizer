package com.mrsep.musicrecognizer.data.remote.audd.rest

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.remote.audd.model.TokenValidationDataStatus
import com.mrsep.musicrecognizer.data.remote.audd.toAuddReturnParameter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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

private const val mediaTypeString = "audio/mpeg; charset=utf-8"
private const val AUDIO_SAMPLE_URL = "https://audd.tech/example.mp3"

class AuddRecognitionService @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    retrofit: Retrofit,
) : RecognitionDataService {
    private val auddClient = retrofit.create<AuddApi>()

    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        file: File
    ): RemoteRecognitionDataResult {
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
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        byteArray: ByteArray
    ): RemoteRecognitionDataResult {
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
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        url: URL
    ): RemoteRecognitionDataResult {
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
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        dataBodyPart: MultipartBody.Builder.() -> MultipartBody.Builder
    ): RemoteRecognitionDataResult {
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("api_token", token)
            .addFormDataPart("return", requiredServices.toAuddReturnParameter())
            .dataBodyPart()
            .build()
        return try {
            auddClient.recognize(multipartBody)
        } catch (e: HttpException) {
            e.printStackTrace()
            RemoteRecognitionDataResult.Error.HttpError(code = e.code(), message = e.message())
        } catch (e: IOException) {
            e.printStackTrace()
            RemoteRecognitionDataResult.Error.BadConnection
        } catch (e: Exception) {
            e.printStackTrace()
            RemoteRecognitionDataResult.Error.UnhandledError(message = e.message ?: "", e = e)
        }
    }

    override suspend fun validateToken(token: String): TokenValidationDataStatus {
        val sampleUrlRecognitionResult = recognize(
            token = token,
            requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
            url = URL(AUDIO_SAMPLE_URL)
        )
        return when (sampleUrlRecognitionResult) {
            is RemoteRecognitionDataResult.Error ->
                TokenValidationDataStatus.Error(sampleUrlRecognitionResult)
            RemoteRecognitionDataResult.NoMatches,
            is RemoteRecognitionDataResult.Success -> TokenValidationDataStatus.Success
        }
    }

}