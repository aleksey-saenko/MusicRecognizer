package com.mrsep.musicrecognizer.data.remote.lyrics

import android.util.Log
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.net.URLEncoder
import javax.inject.Inject

class LyricsFetcherImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    moshi: Moshi
) : LyricsFetcher {

    @OptIn(ExperimentalStdlibApi::class)
    private val ovhJsonAdapter = moshi.adapter<LyricsOvhResponseJson>()

    @OptIn(ExperimentalStdlibApi::class)
    private val lyristJsonAdapter = moshi.adapter<LyristResponseJson>()

    override suspend fun fetch(track: TrackEntity): String? {
        val encodedTitle = URLEncoder.encode(track.title, "UTF-8")
        val encodedArtist = URLEncoder.encode(track.artist, "UTF-8")
        return channelFlow {
            val tasks = listOf(
                async { fetchLyristSource(encodedTitle, encodedArtist) },
                async { fetchOvhSource(encodedTitle, encodedArtist) },
            )
            tasks.forEach { task -> launch { send(task.await()) } }
        }
            .filterNotNull()
            .firstOrNull()
    }

    private suspend fun fetchLyristSource(encodedTitle: String, encodedArtist: String): String? {
        val requestUrl = "https://lyrist.vercel.app/api/:$encodedArtist/:$encodedTitle"
        val request = Request.Builder().url(requestUrl).get().build()
        return try {
            val response = okHttpClient.newCall(request).await()
            if (!response.isSuccessful) return null
            val json = lyristJsonAdapter.fromJson(response.body!!.source())
            json?.lyrics?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Lyrics fetching is failed ($requestUrl)", e)
            null
        }
    }

    private suspend fun fetchOvhSource(encodedTitle: String, encodedArtist: String): String? {
        val requestUrl = "https://api.lyrics.ovh/v1/$encodedArtist/$encodedTitle"
        val request = Request.Builder().url(requestUrl).get().build()
        return try {
            val response = okHttpClient.newCall(request).await()
            if (!response.isSuccessful) return null
            val json = ovhJsonAdapter.fromJson(response.body!!.source())
            json?.lyrics?.run {
                val lineBreak = indexOf("\n")
                if (startsWith("Paroles de la chanson") && lineBreak != -1) {
                    drop(lineBreak + 1)
                } else {
                    this
                }.takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Lyrics fetching is failed ($requestUrl)", e)
            null
        }
    }
}
