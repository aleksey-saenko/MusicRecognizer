package com.mrsep.musicrecognizer.presentation.screens.track

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.TrackRepository
import com.mrsep.musicrecognizer.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val args = TrackScreenArguments(savedStateHandle)

    val uiStateStream = trackRepository.getByMbIdFlow(args.mbId)
        .map { track ->
            track?.let { TrackUiState.Success(track) } ?: TrackUiState.TrackNotFound
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

sealed interface TrackUiState {
    object Loading : TrackUiState
    object TrackNotFound : TrackUiState
    data class Success(val data: Track) : TrackUiState
}