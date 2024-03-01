package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.TrackLink
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
        val track: TrackUi,
        val isMetadataEnhancerRunning: Boolean,
        val artworkBasedThemeEnabled: Boolean,
        val themeMode: ThemeMode,
    ) : TrackUiState()

}

private fun Track.toUiState(
    preferences: UserPreferences,
    isMetadataEnhancerRunning: Boolean
): TrackUiState.Success {
    val trackUi = this.toUi(preferences.requiredMusicServices)
    return TrackUiState.Success(
        track = this.toUi(preferences.requiredMusicServices),
        themeMode = preferences.themeMode,
        artworkBasedThemeEnabled = preferences.artworkBasedThemeEnabled,
        isMetadataEnhancerRunning = isMetadataEnhancerRunning &&
                trackUi.trackLinks.hasMissingLink(preferences.requiredMusicServices),
    )
}

private fun List<TrackLink>.hasMissingLink(requiredServices: List<MusicService>): Boolean {
    val availableServicesSet = map { link -> link.service }.toSet()
    return requiredServices.any { service -> !availableServicesSet.contains(service) }
}

