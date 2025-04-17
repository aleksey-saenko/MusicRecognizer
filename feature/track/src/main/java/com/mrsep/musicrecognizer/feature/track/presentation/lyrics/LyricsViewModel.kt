package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.domain.preferences.LyricsFontStyle
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LyricsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val args = LyricsScreen.Args(savedStateHandle)

    val uiStateStream = combine(
        flow = trackRepository.getTrackFlow(args.trackId),
        flow2 = preferencesRepository.userPreferencesFlow
    ) { track, preferences ->
        track?.let {
            track.lyrics?.let { lyrics ->
                LyricsUiState.Success(
                    trackId = track.id,
                    title = track.title,
                    artist = track.artist,
                    lyrics = lyrics,
                    fontStyle = preferences.lyricsFontStyle,
                    themeSeedColor = track.properties.themeSeedColor,
                    artworkBasedThemeEnabled = preferences.artworkBasedThemeEnabled,
                    themeMode = preferences.themeMode,
                    usePureBlackForDarkTheme = preferences.usePureBlackForDarkTheme,
                    isTrackViewed = track.properties.isViewed,
                    trackDurationMs = track.duration?.inWholeMilliseconds?.toInt()
                )
            }
        } ?: LyricsUiState.LyricsNotFound
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LyricsUiState.Loading
        )

    fun setLyricsFontStyle(newStyle: LyricsFontStyle) {
        viewModelScope.launch {
            preferencesRepository.setLyricsFontStyle(newStyle)
        }
    }

    fun setTrackAsViewed(trackId: String) {
        viewModelScope.launch {
            trackRepository.setViewed(trackId, true)
        }
    }
}

@Immutable
internal sealed class LyricsUiState {

    data object Loading : LyricsUiState()

    data object LyricsNotFound : LyricsUiState()

    data class Success(
        val trackId: String,
        val title: String,
        val artist: String,
        val lyrics: String,
        val fontStyle: LyricsFontStyle,
        val themeSeedColor: Int?,
        val artworkBasedThemeEnabled: Boolean,
        val themeMode: ThemeMode,
        val usePureBlackForDarkTheme: Boolean,
        val isTrackViewed: Boolean,
        val trackDurationMs: Int?,
    ) : LyricsUiState()
}
