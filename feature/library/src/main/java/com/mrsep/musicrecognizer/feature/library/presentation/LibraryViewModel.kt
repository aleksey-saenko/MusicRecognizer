package com.mrsep.musicrecognizer.feature.library.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
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
) : ViewModel() {

    private val _appliedFilterFlow = MutableStateFlow(TrackFilter.Empty)
    val appliedFilterFlow = _appliedFilterFlow.asStateFlow()

    val uiState = combine(
        flow = _appliedFilterFlow,
        flow2 = trackRepository.isEmptyFlow()
    ) { filter, isDatabaseEmpty ->
        if (isDatabaseEmpty) {
            flow<LibraryUiState> { emit(LibraryUiState.EmptyLibrary) }
        } else {
            trackRepository.getFilteredFlow(filter).map { trackList ->
                LibraryUiState.Success(
                    trackList = trackList.toImmutableList(),
                    isFilterApplied = filter != TrackFilter.Empty
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
    val trackSearchResultFlow: StateFlow<SearchResult<Track>> = searchKeywordChannel
        .receiveAsFlow()
        .debounce(SEARCH_INPUT_DEBOUNCE_IN_MS)
        .distinctUntilChanged()
        .flatMapLatest { keyword ->
            if (keyword.isBlank()) {
                flowOf(SearchResult.Success("", emptyList()))
            } else {
                trackRepository.searchResultFlow(keyword, SEARCH_ITEMS_LIMIT)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchResult.Success(
                "",
                emptyList()
            )
        )

    fun submitSearchKeyword(keyword: String) {
        searchKeywordChannel.trySend(
            keyword.takeIf { it.length >= SEARCH_QUERY_MIN_LENGTH } ?: ""
        )
    }

    fun resetSearch() = searchKeywordChannel.trySend("")

    fun applyFilter(trackFilter: TrackFilter) = _appliedFilterFlow.update { trackFilter }

    fun resetFilter() = applyFilter(TrackFilter.Empty)

}

internal sealed class LibraryUiState {

    object Loading : LibraryUiState()

    object EmptyLibrary : LibraryUiState()

    data class Success(
        val trackList: ImmutableList<Track>,
        val isFilterApplied: Boolean
    ) : LibraryUiState()

}

private fun Flow<SearchResult<Track>>.logDebugInfo(): Flow<SearchResult<Track>> {
    return onEach { result ->
        val logMessage =
            "Search result type: ${result::class.simpleName} for keyword=${result.keyword}"
        val addInfo = if (result is SearchResult.Success<Track>)
            "\n" + result.data.joinToString { "${it.title} - ${it.artist}" } else ""
        Log.d("TRACK SEARCH", logMessage + addInfo)
    }
}

