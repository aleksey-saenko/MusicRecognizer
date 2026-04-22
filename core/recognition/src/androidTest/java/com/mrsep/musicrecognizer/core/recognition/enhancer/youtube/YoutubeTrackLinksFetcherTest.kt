package com.mrsep.musicrecognizer.core.recognition.enhancer.youtube

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.recognition.enhancer.RemoteTrackLinks
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.types.shouldBeTypeOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class YoutubeTrackLinksFetcherTest {

    @Test
    fun simpleTest(): Unit = runBlocking {
        val youtubeFetcher = YoutubeTrackLinksFetcher(
            ioDispatcher = Dispatchers.IO,
            httpClientLazy = {
                HttpClient(OkHttp) {
                    install(ContentNegotiation) {
                        json(Json {
                            explicitNulls = false
                            ignoreUnknownKeys = true
                        })
                    }
                }
            }
        )
        val track = Track(
            id = "",
            title = "Rolling in the deep",
            artist = "Adele",
            album = null,
            releaseDate = null,
            duration = null,
            recognizedAt = null,
            recognizedBy = RecognitionProvider.Shazam,
            recognitionDate = Instant.now(),
            lyrics = null,
            artworkThumbUrl = null,
            artworkUrl = null,
            trackLinks = emptyMap(),
            properties = Track.Properties(
                isFavorite = false,
                isViewed = false,
                themeSeedColor = null
            ),
        )
        val result = youtubeFetcher.fetch(track)
        result.shouldBeTypeOf<NetworkResult.Success<RemoteTrackLinks>>()
        result.data.trackLinks.shouldHaveSize(2)
        result.data.trackLinks.shouldContain(MusicService.Youtube, "https://www.youtube.com/watch?v=rYEDA3JcQqw")
        result.data.trackLinks.shouldContain(MusicService.YoutubeMusic, "https://music.youtube.com/watch?v=4ujBQOzs6Lw")
    }
}
