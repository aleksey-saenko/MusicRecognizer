package com.mrsep.musicrecognizer.data.remote.artwork

import com.mrsep.musicrecognizer.data.remote.audd.json.DeezerJson
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import javax.inject.Inject

class ArtworkFetcherImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    moshi: Moshi
) : ArtworkFetcher {

    @OptIn(ExperimentalStdlibApi::class)
    private val deezerJsonAdapter = moshi.adapter<DeezerJson>()

    override suspend fun fetchUrl(track: TrackEntity): String? {
        val deezerTrackLink = track.links.deezer ?: return null
        return fetchDeezerSource(deezerTrackLink)
    }

    private suspend fun fetchDeezerSource(deezerTrackUrl: String): String? {
        val trackIdPattern = Regex("\\d+$")
        val deezerTrackId = trackIdPattern.find(deezerTrackUrl)?.value?.toIntOrNull()
        deezerTrackId ?: return null
        val requestUrl = "https://api.deezer.com/track/$deezerTrackId"
        val request = Request.Builder().url(requestUrl).get().build()
        return try {
            val response = okHttpClient.newCall(request).await()
            if (!response.isSuccessful) return null
            val deezerJson = deezerJsonAdapter.fromJson(response.body!!.source())!!
            deezerJson.album?.run { coverXl ?: coverBig ?: coverMedium }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}