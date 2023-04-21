package com.mrsep.musicrecognizer.feature.library.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun TrackSearchWindow(
    onSearch: (query: String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchClose: () -> Unit,
    searchResult: SearchResult<Track>,
    onTrackClick: (mbId: String) -> Unit
) {
    var text by rememberSaveable {
        mutableStateOf((searchResult as? SearchResult.Success)?.keyword ?: "")
    }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    fun clearFocusAndCloseSearch() {
        focusManager.clearFocus()
        onSearchClose()
    }

    Box(
        modifier
            .semantics { isContainer = true }
            .zIndex(1f)
            .fillMaxWidth()) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .focusRequester(focusRequester),
            query = text,
            onQueryChange = {
                text = it
                onSearch(text)
            },
            onSearch = {
                text = it
                onSearch(text)
            },
            active = true,
            onActiveChange = { active -> if (!active) clearFocusAndCloseSearch() },
            placeholder = { Text(stringResource(StringsR.string.search_track_hint)) },
            leadingIcon = {
                IconButton(onClick = ::clearFocusAndCloseSearch) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = text.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    IconButton(
                        onClick = {
                            text = ""
                            onSearch(text)
                        }
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
                inputFieldColors = inputFieldColors(),
            ),
            tonalElevation = 0.dp
        ) {
            AnimatedContent(targetState = searchResult) { thisSearchResult ->
                when (thisSearchResult) {
                    is SearchResult.Pending -> LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    is SearchResult.Success -> {
                        if (thisSearchResult.keyword == text) {
                            if (thisSearchResult.isEmpty && thisSearchResult.keyword.isNotBlank()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        text = stringResource(StringsR.string.no_tracks_match_search),
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
    }
    var focusRequested by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(focusRequested, searchResult) {
        if ((searchResult as? SearchResult.Success)?.isEmpty == true && !focusRequested) {
            focusRequester.requestFocus()
            focusRequested = true
        }
    }

}