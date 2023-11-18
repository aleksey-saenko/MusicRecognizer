package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder
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
        track?.toUiState(preferences) ?: TrackUiState.TrackNotFound
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

    fun updateThemeSeedColor(mbId: String, color: Int?) {
        viewModelScope.launch {
            trackRepository.updateThemeSeedColor(mbId, color)
        }
    }

}

@Immutable
internal sealed class TrackUiState {

    data object Loading : TrackUiState()

    data object TrackNotFound : TrackUiState()

    data class Success(
        val mbId: String,
        val title: String,
        val artist: String,
        val album: String?,
        val year: String?,
        val lyrics: String?,
        val artworkUrl: String?,
        val isFavorite: Boolean,
        val lastRecognitionDate: String,
        val themeSeedColor: Int?,
        val artworkBasedThemeEnabled: Boolean,
        val themeMode: ThemeMode,
        val odesliLink: String,
        val links: ImmutableList<ServiceLink>
    ) : TrackUiState()

}

private fun Track.toUiState(preferences: UserPreferences): TrackUiState.Success {
    return TrackUiState.Success(
        mbId = this.mbId,
        title = this.title,
        artist = this.artist,
        album = this.album,
        year = this.releaseDate?.year?.toString(),
        lyrics = this.lyrics,
        artworkUrl = this.links.artwork,
        isFavorite = this.metadata.isFavorite,
        lastRecognitionDate = this.metadata.lastRecognitionDate.format(FormatStyle.MEDIUM),
        themeSeedColor = this.metadata.themeSeedColor,
        themeMode = preferences.themeMode,
        artworkBasedThemeEnabled = preferences.artworkBasedThemeEnabled,
        odesliLink = this.createOdesliLink(),
        links = this.links.toUiList(preferences.requiredServices),
    )
}

private fun Instant.format(style: FormatStyle) = this.atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofLocalizedDateTime(style))

internal enum class MusicService {
    Spotify,
    Youtube,
    SoundCloud,
    AppleMusic,
    MusicBrainz,
    Deezer,
    Napster
}

@Immutable
internal data class ServiceLink(
    val type: MusicService,
    val url: String
)

private fun Track.Links.toUiList(required: UserPreferences.RequiredServices): ImmutableList<ServiceLink> {
    val list = mutableListOf<ServiceLink>()
    if (required.spotify) spotify?.let { url -> list.add(ServiceLink(MusicService.Spotify, url)) }
    if (required.youtube) youtube?.let { url -> list.add(ServiceLink(MusicService.Youtube, url)) }
    if (required.soundCloud) soundCloud?.let { url -> list.add(ServiceLink(MusicService.SoundCloud, url)) }
    if (required.appleMusic) appleMusic?.let { url -> list.add(ServiceLink(MusicService.AppleMusic, url)) }
    if (required.musicbrainz) musicBrainz?.let { url -> list.add(ServiceLink(MusicService.MusicBrainz, url)) }
    if (required.deezer) deezer?.let { url -> list.add(ServiceLink(MusicService.Deezer, url)) }
    if (required.napster) napster?.let { url -> list.add(ServiceLink(MusicService.Napster, url)) }
    return list.toImmutableList()
}

private fun Track.createOdesliLink(): String {
    val serviceLink = with(links) { spotify ?: appleMusic ?: deezer ?: napster }
    val query = "?q=${URLEncoder.encode("$artist $title", "UTF-8")}"
    return "https://odesli.co/${serviceLink ?: query}"
}