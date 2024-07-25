package com.mrsep.musicrecognizer.feature.track.presentation.track

import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.track.domain.model.RecognitionProvider
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.TrackLink
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.annotation.concurrent.Immutable

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

internal fun Track.toUi(
    requiredServices: List<MusicService>
): TrackUi {
    val linkMap = trackLinks.associate { link -> link.service to link.url }
    val filteredTrackLinks = requiredServices
        .mapNotNull { service -> linkMap[service]?.let { url -> TrackLink(url, service) } }
        .toImmutableList()
    return TrackUi(
        id = this.id,
        title = this.title,
        artist = this.artist,
        album = this.album,
        year = this.releaseDate?.year?.toString(),
        duration = this.duration?.format(),
        recognizedAt = this.recognizedAt?.format(),
        recognizedBy = this.recognizedBy,
        lyrics = this.lyrics,
        artworkUrl = this.artworkUrl,
        isFavorite = this.isFavorite,
        lastRecognitionDate = this.recognitionDate.format(FormatStyle.MEDIUM),
        themeSeedColor = this.themeSeedColor,
        trackLinks = filteredTrackLinks
    )
}

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
