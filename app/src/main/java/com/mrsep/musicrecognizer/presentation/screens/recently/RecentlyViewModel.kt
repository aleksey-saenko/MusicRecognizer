package com.mrsep.musicrecognizer.presentation.screens.recently

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val RECENTLY_ITEMS_LIMIT = 20
private const val FAVORITE_ITEMS_LIMIT = 20

@HiltViewModel
class RecentlyViewModel @Inject constructor(
    trackRepository: TrackRepository
) : ViewModel() {

    val recentTracksFlow = trackRepository.getLastRecognizedFlow(RECENTLY_ITEMS_LIMIT)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val favoriteTracksFlow = trackRepository.getFavoritesFlow(FAVORITE_ITEMS_LIMIT)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

}