package com.mrsep.musicrecognizer.feature.track.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TrackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackRepository: TrackRepository,
    preferencesRepository: PreferencesRepository,
//    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val args = TrackScreen.Args(savedStateHandle)

    val uiStateStream = trackRepository.getByMbIdFlow(args.mbId)
        .combine(preferencesRepository.userPreferencesFlow) { track, preferences ->
            track?.let { TrackUiState.Success(track, preferences) } ?: TrackUiState.TrackNotFound
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TrackUiState.Loading
        )

    fun onFavoriteClick() {
        viewModelScope.launch {
            val currentState = uiStateStream.value
            if (currentState is TrackUiState.Success) {
                val track = currentState.data
                trackRepository.update(
                    track.copy(
                        metadata = track.metadata.copy(isFavorite = !track.metadata.isFavorite)
                    )
                )
            }
        }
    }

}

internal sealed interface TrackUiState {
    object Loading : TrackUiState
    object TrackNotFound : TrackUiState
    data class Success(
        val data: Track,
        val preferences: UserPreferences
    ) : TrackUiState
}