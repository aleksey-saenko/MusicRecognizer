package com.mrsep.musicrecognizer.feature.library.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.repository.PreferencesRepository
import com.mrsep.musicrecognizer.feature.library.domain.repository.TrackRepository
import com.mrsep.musicrecognizer.feature.library.presentation.model.SearchResultUi
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

private const val SEARCH_ITEMS_LIMIT = 30
private const val SEARCH_INPUT_DEBOUNCE_IN_MS = 300L
private const val SEARCH_QUERY_MIN_LENGTH = 2

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState = combine(
        flow = preferencesRepository.userPreferencesFlow,
        flow2 = trackRepository.isEmptyFlow()
    ) { preferences, isDatabaseEmpty ->
        if (isDatabaseEmpty) {
            flowOf(LibraryUiState.EmptyLibrary)
        } else {
            trackRepository.getFilteredFlow(preferences.trackFilter).map { trackList ->
                LibraryUiState.Success(
                    trackList = trackList.map { track -> track.toUi() }.toImmutableList(),
                    trackFilter = preferences.trackFilter
                )
            }
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState.Loading
        )

    private val searchKeywordChannel = Channel<String>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val trackSearchResultFlow: StateFlow<SearchResultUi> = searchKeywordChannel
        .receiveAsFlow()
        .debounce(SEARCH_INPUT_DEBOUNCE_IN_MS)
        .distinctUntilChanged()
        .flatMapLatest { keyword ->
            if (keyword.isBlank()) {
                flowOf(SearchResultUi.Success("", persistentListOf()))
            } else {
                trackRepository.searchResultFlow(keyword, SEARCH_ITEMS_LIMIT).map { it.toUi() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchResultUi.Success(
                "",
                persistentListOf()
            )
        )

    fun submitSearchKeyword(keyword: String) {
        searchKeywordChannel.trySend(
            keyword.takeIf { it.length >= SEARCH_QUERY_MIN_LENGTH } ?: ""
        )
    }

    fun resetSearch() = searchKeywordChannel.trySend("")

    fun applyFilter(trackFilter: TrackFilter) {
        viewModelScope.launch {
            preferencesRepository.setTrackFilter(trackFilter)
        }
    }

}

internal sealed class LibraryUiState {

    object Loading : LibraryUiState()

    object EmptyLibrary : LibraryUiState()

    data class Success(
        val trackList: ImmutableList<TrackUi>,
        val trackFilter: TrackFilter
    ) : LibraryUiState()

}

private fun Track.toUi() = TrackUi(
    mbId = this.mbId,
    title = this.title,
    artist = this.artist,
    albumAndYear = this.combineAlbumAndYear(),
    artworkUrl = this.artworkUrl
)

private fun SearchResult.toUi(): SearchResultUi = when (this) {
    is SearchResult.Pending -> SearchResultUi.Pending(this.keyword)
    is SearchResult.Success -> SearchResultUi.Success(
        keyword = this.keyword,
        data = this.data.map { it.toUi() }.toImmutableList()
    )
}

private fun Track.combineAlbumAndYear() = this.album?.let { alb ->
    releaseDate?.year?.let { year -> "$alb - $year" } ?: album
}

private fun Flow<SearchResult>.logDebugInfo(): Flow<SearchResult> {
    return onEach { result ->
        val logMessage =
            "Search result type: ${result::class.simpleName} for keyword=${result.keyword}"
        val addInfo = if (result is SearchResult.Success)
            "\n" + result.data.joinToString { "${it.title} - ${it.artist}" } else ""
        Log.d("TRACK SEARCH", logMessage + addInfo)
    }
}

