package com.mrsep.musicrecognizer.presentation.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.SearchResult
import com.mrsep.musicrecognizer.domain.TrackRepository
import com.mrsep.musicrecognizer.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RECENTLY_ITEMS_LIMIT = 50
private const val FAVORITE_ITEMS_LIMIT = 50
private const val SEARCH_ITEMS_LIMIT = 50
private const val SEARCH_INPUT_DEBOUNCE_IN_MS = 300L

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {

    val recentTracksFlow = trackRepository.getLastRecognizedFlow(RECENTLY_ITEMS_LIMIT)
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<Track>().toImmutableList()
        )

    val favoriteTracksFlow = trackRepository.getFavoritesFlow(FAVORITE_ITEMS_LIMIT)
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<Track>().toImmutableList()
        )

    private val searchKeywordChannel = Channel<String>(Channel.CONFLATED)

//    @OptIn(ExperimentalCoroutinesApi::class)
//    val foundTracksFlow = searchKeywordChannel.receiveAsFlow().flatMapLatest { keyword ->
//        if (keyword.isBlank()) {
//            flow { emit(emptyList()) }
//        } else {
//            trackRepository.searchFlow(keyword, 20)
//        }.onEach {
//            Log.d(
//                "SEARCH",
//                "SEARCH RESULTS: ${it.joinToString { "${it.title} - ${it.artist}" }}"
//            )
//        } //debug purpose
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5_000),
//        initialValue = emptyList() //can be null if u wanna get the initial state
//    )

    fun submitSearchKeyword(keyword: String) {
        searchKeywordChannel.trySend(keyword)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val trackSearchResultFlow: StateFlow<SearchResult<Track>> = searchKeywordChannel
        .receiveAsFlow()
        .debounce(SEARCH_INPUT_DEBOUNCE_IN_MS)
        .distinctUntilChanged()
        .flatMapLatest { keyword ->
        if (keyword.isBlank()) {
            flow { emit(SearchResult.Success("", emptyList())) }
        } else {
            trackRepository.searchResultFlow(keyword, SEARCH_ITEMS_LIMIT)
        }
    }
        .onEach { result -> //debug purpose
            val logMessage = "Search result type: ${result::class.simpleName} for keyword=${result.keyword}"
            val addInfo = if (result is SearchResult.Success<Track>)
                "\n" + result.data.joinToString { "${it.title} - ${it.artist}" } else ""
            Log.d("SEARCH", logMessage + addInfo)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchResult.Success(
                "",
                emptyList()
            )
        )

}

sealed class LibraryUiState {
    object Loading : LibraryUiState()
    data class Success(
        val recentsList: List<Track>,
        val favoriteList: List<Track>,
        val foundList: List<Track>,
    ) : LibraryUiState()
}