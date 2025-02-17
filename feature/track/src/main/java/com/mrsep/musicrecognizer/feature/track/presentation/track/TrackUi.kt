package com.mrsep.musicrecognizer.feature.track.presentation.track

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.annotation.concurrent.Immutable

@Immutable
data class TrackLink(
    val url: String,
    val service: MusicService
)

@Immutable
internal data class TrackUi(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val year: String?,
    val duration: String?,
    val recognizedAt: String?,
    val recognizedBy: RecognitionProvider,
    val lyrics: String?,
    val artworkUrl: String?,
    val trackLinks: ImmutableList<TrackLink>,
    val isFavorite: Boolean,
    val lastRecognitionDate: String,
    val themeSeedColor: Int?,
)

internal fun Track.toUi(requiredServices: List<MusicService>) = TrackUi(
    id = id,
    title = title,
    artist = artist,
    album = album,
    year = releaseDate?.year?.toString(),
    duration = duration?.format(),
    recognizedAt = recognizedAt?.format(),
    recognizedBy = recognizedBy,
    lyrics = lyrics,
    artworkUrl = artworkUrl,
    isFavorite = properties.isFavorite,
    lastRecognitionDate = recognitionDate.format(FormatStyle.MEDIUM),
    themeSeedColor = properties.themeSeedColor,
    trackLinks = requiredServices
        .mapNotNull { service -> trackLinks[service]?.let { url -> TrackLink(url, service) } }
        .toImmutableList()
)

private fun Instant.format(style: FormatStyle) = this.atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofLocalizedDateTime(style))

private fun Duration.format(): String {
    val hours = this.toHours()
    val minutes = this.minusHours(hours).toMinutes()
    val seconds = this.minusHours(hours).minusMinutes(minutes).seconds
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
