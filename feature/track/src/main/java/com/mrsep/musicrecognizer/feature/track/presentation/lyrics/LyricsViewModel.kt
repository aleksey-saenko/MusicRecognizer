package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class LyricsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    trackRepository: TrackRepository,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val args = LyricsScreen.Args(savedStateHandle)

    // combine with preferences for future addition of text style control functionality
    val uiStateStream = trackRepository.getLyricsFlowById(args.mbId)
        .combine(preferencesRepository.userPreferencesFlow) { lyrics, preferences ->
            lyrics?.let {
                LyricsUiState.Success(
                    lyrics
                )
            } ?: LyricsUiState.LyricsNotFound
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LyricsUiState.Loading
        )

}

internal sealed class LyricsUiState {
    object Loading : LyricsUiState()
    object LyricsNotFound : LyricsUiState()
    data class Success(
        val lyrics: String
    ) : LyricsUiState()
}