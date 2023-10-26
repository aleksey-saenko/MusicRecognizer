package com.mrsep.musicrecognizer.feature.library.presentation.search

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val SEARCH_ITEMS_LIMIT = 30
private const val SEARCH_INPUT_DEBOUNCE_IN_MS = 400L
private const val SEARCH_QUERY_MIN_LENGTH = 2
private const val KEY_QUERY = "KEY_QUERY"

@HiltViewModel
internal class LibrarySearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackRepository: TrackRepository
) : ViewModel() {

    val query = savedStateHandle.getStateFlow(KEY_QUERY, "")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val trackSearchResultFlow: StateFlow<SearchResultUi> = query
        .map { query -> if (query.length >= SEARCH_QUERY_MIN_LENGTH) query else "" }
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
        savedStateHandle[KEY_QUERY] = keyword
    }

    fun resetSearch() {
        savedStateHandle[KEY_QUERY] = ""
    }

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