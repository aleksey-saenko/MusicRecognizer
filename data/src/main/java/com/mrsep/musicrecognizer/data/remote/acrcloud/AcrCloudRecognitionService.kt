package com.mrsep.musicrecognizer.data.remote.acrcloud

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionServiceDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.acrcloud.json.AcrCloudResponseJson
import com.mrsep.musicrecognizer.data.remote.acrcloud.json.toRecognitionResult
import com.mrsep.musicrecognizer.data.remote.artwork.ArtworkFetcher
import com.mrsep.musicrecognizer.data.remote.lyrics.LyricsFetcher
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await
import java.io.File
import java.io.IOException
import java.net.URL
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal class AcrCloudRecognitionService @AssistedInject constructor(
    @Assisted private val config: AcrCloudConfigDo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val artworkFetcher: ArtworkFetcher,
    private val lyricsFetcher: LyricsFetcher,
    private val okHttpClient: OkHttpClient,
    moshi: Moshi,
) : RecognitionServiceDo {

    @OptIn(ExperimentalStdlibApi::class)
    private val acrJsonAdapter = moshi.adapter<AcrCloudResponseJson>()

    override suspend fun recognize(recording: ByteArray): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            val timestamp = Instant.now().epochSecond.toString()
            val signature = createSignature(timestamp, config)
            val multipartBody = MultipartBody.Builder()
                .addFormDataPart(
                    "sample",
                    "sample",
                    recording.toRequestBody(RECORDING_MEDIA_TYPE.toMediaTypeOrNull())
                )
                .setType(MultipartBody.FORM)
                .addFormDataPart("access_key", config.accessKey)
                .addFormDataPart("sample_bytes", "${recording.size}")
                .addFormDataPart("timestamp", timestamp)
                .addFormDataPart("signature", signature)
                .addFormDataPart("data_type", DATA_TYPE)
                .addFormDataPart("signature_version", SIGNATURE_VERSION)
                .build()

            val request = Request.Builder()
                .url("https://${config.host}$ENDPOINT")
                .post(multipartBody)
                .build()
            val response = try {
                okHttpClient.newCall(request).await()
            } catch (e: IOException) {
                return@withContext RemoteRecognitionResultDo.Error.BadConnection
            }
            response.use {
                if (response.isSuccessful) {
                    val remoteResult = try {
                        val responseJson = acrJsonAdapter.fromJson(response.body!!.source())!!
                        responseJson.toRecognitionResult()
                    } catch (e: Exception) {
                        RemoteRecognitionResultDo.Error.UnhandledError(
                            message = e.message ?: "",
                            e = e
                        )
                    }
                    if (remoteResult is RemoteRecognitionResultDo.Success) {
                        val track = remoteResult.data
                        val (artworkUrl, lyrics) = listOf(
                            async { artworkFetcher.fetchUrl(track) },
                            async { lyricsFetcher.fetch(track) }
                        ).awaitAll()
                        val finalTrack = track.copy(
                            lyrics = lyrics,
                            links = track.links.copy(
                                artwork = artworkUrl
                            )
                        )
                        RemoteRecognitionResultDo.Success(finalTrack)
                    } else {
                        remoteResult
                    }
                } else {
                    RemoteRecognitionResultDo.Error.HttpError(
                        code = response.code,
                        message = response.message
                    )
                }
            }
        }
    }

    override suspend fun recognize(recording: File): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            val bytes = recording.readBytes()
            recognize(bytes)
        }
    }

    override suspend fun recognize(url: URL): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            val request = Request.Builder().url(url).build()
            val response = try {
                okHttpClient.newCall(request).await()
            } catch (e: IOException) {
                return@withContext RemoteRecognitionResultDo.Error.BadConnection
            }
            response.use {
                val recording = response.body!!.bytes().takeIf { it.isNotEmpty() }
                if (recording != null) {
                    recognize(recording)
                } else {
                    val message = "Remote file is unavailable [$url]"
                    RemoteRecognitionResultDo.Error.BadRecording(message)
                }
            }
        }
    }

    override suspend fun recognize(
        recordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            var retryCounter = 0
            // initially bad connection
            // for case when after TIMEOUT_AFTER_RECORDING_FINISHED there is no any response yet
            var lastResult: RemoteRecognitionResultDo =
                RemoteRecognitionResultDo.Error.BadConnection

            val recordingChannel = Channel<ByteArray>(Channel.UNLIMITED)
            val recordWithTimeoutJob = launch {
                recordingFlow.collect(recordingChannel::send)
                recordingChannel.close()
                delay(TIMEOUT_AFTER_RECORDING_FINISHED)
            }

            val remoteResultJob = recordingChannel.receiveAsFlow()
                .onEmpty {
                    val msg = "Recognition process failed due to empty audio recording stream"
                    lastResult = RemoteRecognitionResultDo.Error.BadRecording(msg)
                }
                .transformWhile<ByteArray, Unit> { recording ->
                    lastResult = recognize(recording)
                    while (lastResult.isRetryRequired() && retryCounter < RETRY_ON_ERROR_LIMIT) {
                        retryCounter++
                        lastResult = recognize(recording)
                    }
                    // continue recognition only if no matches
                    lastResult is RemoteRecognitionResultDo.NoMatches
                }
                .launchIn(this)

            select {
                remoteResultJob.onJoin { recordWithTimeoutJob.cancelAndJoin() }
                recordWithTimeoutJob.onJoin { remoteResultJob.cancelAndJoin() }
            }

            lastResult
        }
    }

    private fun RemoteRecognitionResultDo.isRetryRequired() = when (this) {
        is RemoteRecognitionResultDo.Success,
        RemoteRecognitionResultDo.NoMatches,
        is RemoteRecognitionResultDo.Error.BadRecording,
        is RemoteRecognitionResultDo.Error.AuthError,
        is RemoteRecognitionResultDo.Error.ApiUsageLimited,
        is RemoteRecognitionResultDo.Error.UnhandledError -> false

        RemoteRecognitionResultDo.Error.BadConnection,
        is RemoteRecognitionResultDo.Error.HttpError -> true
    }

    private fun createSignature(timestamp: String, config: AcrCloudConfigDo): String {
        val sigString = METHOD + "\n" + ENDPOINT + "\n" + config.accessKey +
                "\n" + DATA_TYPE + "\n" + SIGNATURE_VERSION + "\n" + timestamp
        return encryptByHMACSHA1(sigString.toByteArray(), config.accessSecret.toByteArray())
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encryptByHMACSHA1(data: ByteArray, key: ByteArray): String {
        return try {
            val signingKey = SecretKeySpec(key, "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(signingKey)
            val rawHmac = mac.doFinal(data)
            Base64.Default.encode(rawHmac)
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Error during signature encryption", e)
            ""
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(config: AcrCloudConfigDo): AcrCloudRecognitionService
    }

    companion object {
        private const val METHOD = "POST"
        private const val ENDPOINT = "/v1/identify"
        private const val DATA_TYPE = "audio"
        private const val SIGNATURE_VERSION = "1"

        private const val RECORDING_MEDIA_TYPE = "audio/mpeg; charset=utf-8"

        // binary (acr cloud docs)
//        private const val RECORDING_MEDIA_TYPE = "application/octet-stream"
        private const val RETRY_ON_ERROR_LIMIT = 1
        private const val TIMEOUT_AFTER_RECORDING_FINISHED = 10_000L
    }
}
