package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.domain.preferences.LyricsStyle
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.core.domain.track.model.Lyrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
internal class LyricsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository,
    trackMetadataEnhancerScheduler: TrackMetadataEnhancerScheduler,
) : ViewModel() {

    private val args = LyricsScreen.Args(savedStateHandle)

    val uiStateStream = combine(
        flow = trackRepository.getTrackFlow(args.trackId),
        flow2 = preferencesRepository.userPreferencesFlow,
        flow3 = trackMetadataEnhancerScheduler.isLyricsFetcherRunning(args.trackId)
    ) { track, preferences, isLyricsFetcherRunning ->
        if (track == null) return@combine LyricsUiState.TrackNotFound
        if (isLyricsFetcherRunning) return@combine LyricsUiState.Loading
        track.lyrics?.let { lyrics ->
            LyricsUiState.Success(
                trackId = track.id,
                title = track.title,
                artist = track.artist,
                artworkUrl = track.artworkUrl,
                lyrics = lyrics,
                trackDuration = track.duration,
                recognizedAt = track.recognizedAt,
                recognitionDate = track.recognitionDate,
                lyricsStyle = preferences.lyricsStyle,
                themeSeedColor = track.properties.themeSeedColor,
                artworkBasedThemeEnabled = preferences.artworkBasedThemeEnabled,
                themeMode = preferences.themeMode,
                usePureBlackForDarkTheme = preferences.usePureBlackForDarkTheme,
                isTrackViewed = track.properties.isViewed,
            )
        } ?: LyricsUiState.LyricsNotFound
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LyricsUiState.Loading
        )

    fun setLyricsStyle(newStyle: LyricsStyle) {
        viewModelScope.launch {
            preferencesRepository.setLyricsStyle(newStyle)
        }
    }

    fun setTrackAsViewed(trackId: String) {
        viewModelScope.launch {
            trackRepository.setViewed(trackId, true)
        }
    }

    fun setThemeSeedColor(trackId: String, color: Int?) {
        viewModelScope.launch {
            trackRepository.setThemeSeedColor(trackId, color)
        }
    }
}

@Immutable
internal sealed class LyricsUiState {

    data object Loading : LyricsUiState()

    data object TrackNotFound : LyricsUiState()

    data object LyricsNotFound : LyricsUiState()

    data class Success(
        val trackId: String,
        val title: String,
        val artist: String,
        val artworkUrl: String?,
        val lyrics: Lyrics,
        val trackDuration: Duration?,
        val recognizedAt: Duration?,
        val recognitionDate: Instant,
        val lyricsStyle: LyricsStyle,
        val themeSeedColor: Int?,
        val artworkBasedThemeEnabled: Boolean,
        val themeMode: ThemeMode,
        val usePureBlackForDarkTheme: Boolean,
        val isTrackViewed: Boolean,
    ) : LyricsUiState()
}
