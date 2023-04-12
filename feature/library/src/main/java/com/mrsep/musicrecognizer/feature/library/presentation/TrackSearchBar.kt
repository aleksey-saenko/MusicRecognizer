package com.mrsep.musicrecognizer.feature.library.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.R
import com.mrsep.musicrecognizer.feature.library.domain.model.Track

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun TrackSearchBar(
    onSearch: (query: String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchClose: () -> Unit,
    searchResult: SearchResult<Track>,
    onTrackClick: (mbId: String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

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
            onActiveChange = {},
            placeholder = { Text("Search track...") },
            leadingIcon = {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        focusManager.clearFocus()
                        onSearch("")
                        onSearchClose()
                    }
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = text.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            text = ""
                            onSearch(text)
                        }
                    )
                }
            },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                dividerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                inputFieldColors = inputFieldColors(),
            ),
            tonalElevation = 0.dp
        ) {
            AnimatedContent(targetState = searchResult) { localSearchResult ->
                when (localSearchResult) {
                    is SearchResult.Processing -> LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    is SearchResult.Success -> LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(localSearchResult.data.size) { index ->
                            TrackSearchItem(
                                track = localSearchResult.data[index],
                                onTrackClick = onTrackClick
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(searchResult) {
        if ((searchResult as? SearchResult.Success)?.isEmpty == true) {
            focusRequester.requestFocus()
        }
    }
}