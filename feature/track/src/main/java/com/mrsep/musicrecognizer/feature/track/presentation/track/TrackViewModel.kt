package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.preferences.UserPreferences
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
internal class TrackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferencesRepository: PreferencesRepository,
    private val trackRepository: TrackRepository,
    trackMetadataEnhancerScheduler: TrackMetadataEnhancerScheduler,
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
            trackRepository.delete(listOf(trackId))
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

    fun setTrackAsViewed(trackId: String) {
        viewModelScope.launch {
            trackRepository.setViewed(trackId, true)
        }
    }
}

@Immutable
internal sealed class TrackUiState {

    data object Loading : TrackUiState()

    data object TrackNotFound : TrackUiState()

    data class Success(
        val track: TrackUi,
        val isTrackViewed: Boolean,
        val isMetadataEnhancerRunning: Boolean,
        val artworkBasedThemeEnabled: Boolean,
        val themeMode: ThemeMode,
        val usePureBlackForDarkTheme: Boolean,
    ) : TrackUiState()
}

private fun Track.toUiState(
    preferences: UserPreferences,
    isMetadataEnhancerRunning: Boolean
): TrackUiState.Success {
    val trackUi = this.toUi(preferences.requiredMusicServices)
    return TrackUiState.Success(
        track = this.toUi(preferences.requiredMusicServices),
        isTrackViewed = this.properties.isViewed,
        isMetadataEnhancerRunning = isMetadataEnhancerRunning &&
                trackUi.trackLinks.size < preferences.requiredMusicServices.size,
        artworkBasedThemeEnabled = preferences.artworkBasedThemeEnabled,
        themeMode = preferences.themeMode,
        usePureBlackForDarkTheme = preferences.usePureBlackForDarkTheme,
    )
}
