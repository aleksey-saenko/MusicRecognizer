package com.mrsep.musicrecognizer.feature.library.presentation.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibrarySearchScreen(
    onBackPressed: () -> Unit,
    onTrackClick: (trackId: String) -> Unit,
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
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .systemBarsPadding()
    ) {
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
                        painter = painterResource(UiR.drawable.outline_arrow_back_24),
                        contentDescription = stringResource(StringsR.string.back)
                    )
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotBlank(),
                    enter = fadeIn(spring()),
                    exit = fadeOut(spring()),
                ) {
                    IconButton(
                        onClick = viewModel::resetSearch
                    ) {
                        Icon(
                            painter = painterResource(UiR.drawable.outline_close_24),
                            contentDescription = stringResource(StringsR.string.clear_search_query)
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
            SearchResultLazyColumn(
                query = query,
                searchResult = searchResult,
                onTrackClick = onTrackClick,
                modifier = Modifier.fillMaxSize()
            )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultLazyColumn(
    query: String,
    searchResult: SearchResultUi,
    onTrackClick: (trackId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = searchResult,
        transitionSpec = { fadeIn(spring()).togetherWith(fadeOut(spring())) },
        contentAlignment = Alignment.TopCenter,
        label = "SearchResult",
        modifier = modifier,
    ) { result ->
        when (result) {
            is SearchResultUi.Pending -> Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Round
                )
            }

            is SearchResultUi.Success -> Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = result.keyword == query,
                    enter = fadeIn(spring()),
                    exit = fadeOut(spring()),
                    label = "SearchResult"
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = result.data,
                            key = { track -> track.id }
                        ) { track ->
                            TrackSearchItem(
                                track = track,
                                keyword = result.keyword,
                                onClick = { onTrackClick(track.id) },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = result.isEmpty && result.keyword.isNotBlank(),
                    enter = fadeIn(spring()),
                    exit = fadeOut(spring()),
                    label = "NotFoundMessage"
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.background)
                            .fillMaxSize(),
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
                }
            }

        }
    }
}