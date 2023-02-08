package com.mrsep.musicrecognizer.data.remote.audd

import android.content.Context
import com.mrsep.musicrecognizer.BuildConfig
import com.mrsep.musicrecognizer.domain.RecognizeService
import com.mrsep.musicrecognizer.domain.model.RecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.create
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val token = BuildConfig.AUDD_TOKEN
private const val returnParam =
    "lyrics,apple_music,spotify,deezer,napster,musicbrainz" //lyrics,apple_music,spotify,deezer,napster,musicbrainz
private const val mediaTypeString = "audio/mpeg; charset=utf-8"

@Singleton
class AuddRecognizeService @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val moshi: Moshi,
    retrofit: Retrofit,
) : RecognizeService {
    private val auddClient = retrofit.create<AuddApi>()

    override suspend fun recognize(file: File): RecognizeResult<Track> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("api_token", token)
                .addFormDataPart("return", returnParam)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody(mediaTypeString.toMediaTypeOrNull())
                )
                .build()
            return@withContext try {
                auddClient.recognize(multipartBody)
            } catch (e: HttpException) {
                e.printStackTrace()
                RecognizeResult.Error.HttpError(code = e.code(), message = e.message())
            } catch (e: IOException) {
                e.printStackTrace()
                RecognizeResult.Error.NoConnection
            } catch (e: Exception) {
                e.printStackTrace()
                RecognizeResult.Error.UnhandledError(message = e.message ?: "", e = e)
            }
        }
    }

    override suspend fun fakeRecognize(): RecognizeResult<Track> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            delay(1_000)
            val fakeJson = appContext.assets.open("fake_json.txt").bufferedReader().use {
                it.readText()
            }
            val resultType = Types.newParameterizedType(
                RecognizeResult::class.java,
                Track::class.java
            )
            moshi.adapter<RecognizeResult<Track>>(resultType).fromJson(fakeJson)!!
        }
    }

}