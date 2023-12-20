package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.track.domain.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.track.domain.model.TrackLink
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
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
    private val trackMetadataEnhancerScheduler: TrackMetadataEnhancerScheduler
) : ViewModel() {

    private val args = TrackScreen.Args(savedStateHandle)

    private var trackRemovalRequested = false

    private val _trackExistingState = MutableStateFlow(true)
    val trackExistingState = _trackExistingState.asStateFlow()

    val uiStateStream = combine(
        trackRepository.getTrackFlow(args.trackId),
        preferencesRepository.userPreferencesFlow,
        trackMetadataEnhancerScheduler.isRunning(args.trackId)
    ) { track, preferences, isMetaEnhancerRunning ->
        track?.toUiState(preferences, isMetaEnhancerRunning) ?: TrackUiState.TrackNotFound
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


    fun deleteTrack(trackId: String) {
        trackRemovalRequested = true
        viewModelScope.launch {
            trackMetadataEnhancerScheduler.cancel(trackId)
            trackRepository.delete(trackId)
            _trackExistingState.update { false }
        }
    }

    fun setFavorite(trackId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            trackRepository.setFavorite(trackId, isFavorite)
        }
    }

    fun setThemeSeedColor(trackId: String, color: Int?) {
        viewModelScope.launch {
            trackRepository.setThemeSeedColor(trackId, color)
        }
    }

}

@Immutable
internal sealed class TrackUiState {

    data object Loading : TrackUiState()

    data object TrackNotFound : TrackUiState()

    data class Success(
        val trackId: String,
        val title: String,
        val artist: String,
        val album: String?,
        val year: String?,
        val lyrics: String?,
        val artworkUrl: String?,
        val requiredLinks: ImmutableList<TrackLink>,
        val isLoadingLinks: Boolean,
        val isFavorite: Boolean,
        val lastRecognitionDate: String,
        val themeSeedColor: Int?,
        val artworkBasedThemeEnabled: Boolean,
        val themeMode: ThemeMode,
    ) : TrackUiState()

}

private fun Track.toUiState(
    preferences: UserPreferences,
    isMetadataEnhancerRunning: Boolean
): TrackUiState.Success {
    val linkMap = trackLinks.associate { link -> link.service to link.url }
    val requiredLinks = preferences.requiredMusicServices
        .mapNotNull { service -> linkMap[service]?.let { url -> TrackLink(url, service) } }
        .toImmutableList()
    return TrackUiState.Success(
        trackId = this.id,
        title = this.title,
        artist = this.artist,
        album = this.album,
        year = this.releaseDate?.year?.toString(),
        lyrics = this.lyrics,
        artworkUrl = this.artworkUrl,
        isFavorite = this.properties.isFavorite,
        lastRecognitionDate = this.properties.lastRecognitionDate.format(FormatStyle.MEDIUM),
        themeSeedColor = this.properties.themeSeedColor,
        themeMode = preferences.themeMode,
        artworkBasedThemeEnabled = preferences.artworkBasedThemeEnabled,
        requiredLinks = requiredLinks,
        isLoadingLinks = isMetadataEnhancerRunning &&
                requiredLinks.hasMissingLink(preferences.requiredMusicServices)
    )
}

private fun List<TrackLink>.hasMissingLink(requiredServices: List<MusicService>): Boolean {
    val availableServicesSet = map { link -> link.service }.toSet()
    return requiredServices.any { service -> !availableServicesSet.contains(service) }
}

private fun Instant.format(style: FormatStyle) = this.atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofLocalizedDateTime(style))