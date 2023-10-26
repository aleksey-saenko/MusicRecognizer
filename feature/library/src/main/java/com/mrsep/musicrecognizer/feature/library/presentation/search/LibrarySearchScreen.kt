package com.mrsep.musicrecognizer.feature.library.presentation.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibrarySearchScreen(
    onBackPressed: () -> Unit,
    onTrackClick: (mbId: String) -> Unit,
    viewModel: LibrarySearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResult by viewModel.trackSearchResultFlow.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    fun clearFocusAndCloseSearch() {
        focusManager.clearFocus()
        onBackPressed()
    }

    Box(
        Modifier
            .semantics { isTraversalGroup = true }
            .zIndex(1f)
            .fillMaxWidth()) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .focusRequester(focusRequester),
            query = query,
            onQueryChange = viewModel::submitSearchKeyword,
            onSearch = viewModel::submitSearchKeyword,
            active = true,
            onActiveChange = { active -> if (!active) clearFocusAndCloseSearch() },
            placeholder = { Text(stringResource(StringsR.string.search_track_hint)) },
            leadingIcon = {
                IconButton(onClick = ::clearFocusAndCloseSearch) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(StringsR.string.back)
                    )
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    IconButton(
                        onClick = viewModel::resetSearch
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                }
            },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                dividerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                inputFieldColors = SearchBarDefaults.inputFieldColors(),
            ),
            tonalElevation = 0.dp
        ) {
            AnimatedContent(
                targetState = searchResult,
                label = "SearchResult"
            ) { thisSearchResult ->
                when (thisSearchResult) {
                    is SearchResultUi.Pending -> LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )

                    is SearchResultUi.Success -> {
                        if (thisSearchResult.keyword != query) return@AnimatedContent
                        if (thisSearchResult.isEmpty && thisSearchResult.keyword.isNotBlank()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = stringResource(StringsR.string.no_tracks_match_search),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .padding(top = 24.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(thisSearchResult.data.size) { index ->
                                    TrackSearchItem(
                                        track = thisSearchResult.data[index],
                                        keyword = thisSearchResult.keyword,
                                        onTrackClick = onTrackClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    var focusRequested by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(focusRequested, searchResult) {
        if ((searchResult as? SearchResultUi.Success)?.isEmpty == true && !focusRequested) {
            focusRequester.requestFocus()
            focusRequested = true
        }
    }
}