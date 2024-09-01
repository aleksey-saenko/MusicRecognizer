package com.mrsep.musicrecognizer.data

import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

internal val fakeTrack = TrackEntity(
    id = UUID.randomUUID().toString(),
    title = "Echoes",
    artist = "Pink Floyd",
    album = "Meddle",
    releaseDate = LocalDate.parse("1971-10-30", DateTimeFormatter.ISO_DATE),
    duration = null,
    recognizedAt = null,
    recognizedBy = RecognitionProviderDo.Audd,
    recognitionDate = Instant.now(),
    lyrics = "lyrics stub",
    links = TrackEntity.Links(
        artworkThumbnail = "https://e-cdns-images.dzcdn.net/images/cover/dce2b0c4e6498a136df041df7e85aba3/250x250-000000-80-0-0.jpg",
        artwork = "https://e-cdns-images.dzcdn.net/images/cover/dce2b0c4e6498a136df041df7e85aba3/1000x1000-000000-80-0-0.jpg",
        amazonMusic = null,
        anghami = null,
        appleMusic = null,
        audiomack = null,
        audius = null,
        boomplay = null,
        deezer = null,
        musicBrainz = null,
        napster = null,
        pandora = null,
        soundCloud = null,
        spotify = null,
        tidal = null,
        yandexMusic = null,
        youtube = null,
        youtubeMusic = null
    ),
    properties = TrackEntity.Properties(
        isFavorite = false,
        isViewed = false,
        themeSeedColor = null,
    ),
)

internal fun emptyAudioRecordingFlow(delayBeforeClose: Long) = flow<ByteArray> {
    delay(delayBeforeClose)
}

internal const val normalAudioFlowDuration = 5000L
internal const val normalAudioDelay1 = 0L
internal const val normalAudioDelay2 = 1000L
internal const val normalAudioDelay3 = 1500L
internal const val normalAudioDelay4 = 1000L
internal const val normalAudioDelay5 = 1500L
internal val normalAudioRecordingFlow get() = flow {
    delay(normalAudioDelay1)
    emit(ByteArray(100 * 1024))
    println("Emitted rec #1")
    delay(normalAudioDelay2)
    emit(ByteArray(200 * 1024))
    println("Emitted rec #2")
    delay(normalAudioDelay3)
    emit(ByteArray(300 * 1024))
    println("Emitted rec #3")
    delay(normalAudioDelay4)
    emit(ByteArray(400 * 1024))
    println("Emitted rec #4")
    delay(normalAudioDelay5)
    emit(ByteArray(500 * 1024))
    println("Emitted rec #5")
}
