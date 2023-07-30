package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
internal class TrackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackRepository: TrackRepository,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val args = TrackScreen.Args(savedStateHandle)

    private var trackRemovalRequested = false

    private val _trackExistingState = MutableStateFlow(true)
    val trackExistingState = _trackExistingState.asStateFlow()

    val uiStateStream = combine(
        trackRepository.getByMbIdFlow(args.mbId),
        preferencesRepository.userPreferencesFlow
    ) { track, preferences ->
        track?.toUiState(preferences.requiredServices) ?: TrackUiState.TrackNotFound
    }.transformWhile { uiState ->
        // do not update track state if track removal was requested
        // after track deletion and final animation, the screen should be destroyed
        if (!trackRemovalRequested) emit(uiState)
        !trackRemovalRequested
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TrackUiState.Loading
    )


    fun deleteTrack(mbId: String) {
        trackRemovalRequested = true
        viewModelScope.launch {
            trackRepository.deleteByMbId(mbId)
            _trackExistingState.update { false }
        }
    }

    fun toggleFavoriteMark(mbId: String) {
        viewModelScope.launch {
            trackRepository.toggleFavoriteMark(mbId)
        }
    }

}

@Immutable
internal sealed interface TrackUiState {
    data object Loading : TrackUiState
    data object TrackNotFound : TrackUiState
    data class Success(
        val mbId: String,
        val title: String,
        val artist: String,
        val albumAndYear: String?,
        val artworkUrl: String?,
        val isFavorite: Boolean,
        val isLyricsAvailable: Boolean,
        val lastRecognitionDate: String,
        val links: ImmutableList<ServiceLink>
    ) : TrackUiState {

        val sharedBody
            get() = albumAndYear?.let { albAndYear ->
                "$title / $artist / $albAndYear"
            } ?: "$title / $artist"
    }

}

private fun Track.toUiState(
    requiredServices: UserPreferences.RequiredServices
): TrackUiState.Success {
    return TrackUiState.Success(
        mbId = this.mbId,
        title = this.title,
        artist = this.artist,
        albumAndYear = this.combineAlbumAndYear(),
        artworkUrl = this.links.artwork,
        isFavorite = this.metadata.isFavorite,
        isLyricsAvailable = this.lyrics != null,
        lastRecognitionDate = this.metadata.lastRecognitionDate.format(FormatStyle.MEDIUM),
        links = this.links.toUiList(requiredServices),
    )
}

private fun Instant.format(style: FormatStyle) = this.atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofLocalizedDateTime(style))

private fun Track.combineAlbumAndYear() = this.album?.let { alb ->
    releaseDate?.year?.let { year -> "$alb - $year" } ?: album
}

@Immutable
internal sealed interface ServiceLink {
    val url: String

    @JvmInline
    value class Spotify(override val url: String) : ServiceLink

    @JvmInline
    value class Youtube(override val url: String) : ServiceLink

    @JvmInline
    value class SoundCloud(override val url: String) : ServiceLink

    @JvmInline
    value class AppleMusic(override val url: String) : ServiceLink

    @JvmInline
    value class MusicBrainz(override val url: String) : ServiceLink

    @JvmInline
    value class Deezer(override val url: String) : ServiceLink

    @JvmInline
    value class Napster(override val url: String) : ServiceLink
}

private fun Track.Links.toUiList(required: UserPreferences.RequiredServices): ImmutableList<ServiceLink> {
    val list = mutableListOf<ServiceLink>()
    if (required.spotify) spotify?.let { url -> list.add(ServiceLink.Spotify(url)) }
    if (required.youtube) youtube?.let { url -> list.add(ServiceLink.Youtube(url)) }
    if (required.soundCloud) soundCloud?.let { url -> list.add(ServiceLink.SoundCloud(url)) }
    if (required.appleMusic) appleMusic?.let { url -> list.add(ServiceLink.AppleMusic(url)) }
    if (required.musicbrainz) musicBrainz?.let { url -> list.add(ServiceLink.MusicBrainz(url)) }
    if (required.deezer) deezer?.let { url -> list.add(ServiceLink.Deezer(url)) }
    if (required.napster) napster?.let { url -> list.add(ServiceLink.Napster(url)) }
    return list.toImmutableList()
}