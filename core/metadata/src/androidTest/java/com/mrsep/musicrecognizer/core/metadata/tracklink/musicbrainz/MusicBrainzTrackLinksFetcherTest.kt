package com.mrsep.musicrecognizer.core.metadata.tracklink.musicbrainz

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.metadata.tracklink.RemoteTrackLinks
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
class MusicBrainzTrackLinksFetcherTest {

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

    private val linkFetcher = MusicBrainzTrackLinksFetcher(
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
    )

    @Test
    fun testWithIsrc(): Unit = runBlocking {
        val result = linkFetcher.fetch(track)

        result.shouldBeTypeOf<NetworkResult.Success<RemoteTrackLinks>>()
        with(result.data.trackLinks) {
            shouldHaveSize(1)
            shouldContain(MusicService.MusicBrainz, "https://musicbrainz.org/recording/1a13c710-4b7e-4701-8968-cd61f2e58110")
        }
    }

    @Test
    fun testWithoutIsrc(): Unit = runBlocking {
        val track = track.copy(isrc = null)
        val result = linkFetcher.fetch(track)

        result.shouldBeTypeOf<NetworkResult.Success<RemoteTrackLinks>>()
        with(result.data.trackLinks) {
            shouldHaveSize(1)
            shouldContain(MusicService.MusicBrainz, "https://musicbrainz.org/recording/1a13c710-4b7e-4701-8968-cd61f2e58110")
        }
    }
}
