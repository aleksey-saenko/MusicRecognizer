package com.mrsep.musicrecognizer.core.recognition.enhancer.qobuz

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
import java.util.Locale

// Device locale (localeProvider) affects the resulting track link (Qobuz track id)

@RunWith(AndroidJUnit4::class)
class QobuzTrackLinksFetcherTest {

    private val track = Track(
        id = "id",
        title = "Rolling in the deep",
        artist = "Adele",
        album = "21",
        releaseDate = null,
        duration = null,
        recognizedAt = null,
        recognizedBy = RecognitionProvider.Shazam,
        recognitionDate = Instant.now(),
        lyrics = null,
        isrc = "GBBKS1000335",
        artworkThumbUrl = null,
        artworkUrl = null,
        trackLinks = emptyMap(),
        properties = Track.Properties(
            isFavorite = false,
            isViewed = false,
            themeSeedColor = null
        ),
    )

    private val qobuzFetcher = QobuzTrackLinksFetcher(
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
        },
        localeProvider = { Locale.of("de", "DE") }
    )

    @Test
    fun testWithIsrc(): Unit = runBlocking {
        val result = qobuzFetcher.fetch(track)

        result.shouldBeTypeOf<NetworkResult.Success<RemoteTrackLinks>>()
        with(result.data.trackLinks) {
            shouldHaveSize(1)
            shouldContain(MusicService.Qobuz, "https://open.qobuz.com/track/3147371")
        }
    }

    @Test
    fun testWithoutIsrc(): Unit = runBlocking {
        val track = track.copy(isrc = null)
        val result = qobuzFetcher.fetch(track)

        result.shouldBeTypeOf<NetworkResult.Success<RemoteTrackLinks>>()
        with(result.data.trackLinks) {
            shouldHaveSize(1)
            shouldContain(MusicService.Qobuz, "https://open.qobuz.com/track/3147371")
        }
    }
}
