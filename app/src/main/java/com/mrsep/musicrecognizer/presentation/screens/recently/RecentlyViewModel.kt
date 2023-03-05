package com.mrsep.musicrecognizer.presentation.screens.recently

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RECENTLY_ITEMS_LIMIT = 20
private const val FAVORITE_ITEMS_LIMIT = 20
private const val SEARCH_ITEMS_LIMIT = 20

@HiltViewModel
class RecentlyViewModel @Inject constructor(
    private val trackRepository: TrackRepository
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

    private val searchKeywordChannel = Channel<String>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class)
    val foundTracksFlow = searchKeywordChannel.receiveAsFlow().flatMapLatest { keyword ->
        if (keyword.isBlank()) {
            flow { emit(emptyList()) }
        } else {
            trackRepository.searchFlow(keyword, 20)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList() //can be null if u wanna get the initial state
    )

    fun submitSearchKeyword(keyword: String) {
        viewModelScope.launch { searchKeywordChannel.send(keyword) }
    }

}