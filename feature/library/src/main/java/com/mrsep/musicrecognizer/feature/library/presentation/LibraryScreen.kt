package com.mrsep.musicrecognizer.feature.library.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import kotlinx.coroutines.launch

private const val animationDuration = 300

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onTrackClick: (mbId: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val screenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLibraryEmpty = screenUiState !is LibraryUiState.Success
    var searchWindowActive by rememberSaveable { mutableStateOf(false) }
    var filterSheetActive by rememberSaveable { mutableStateOf(false) }

    AnimatedContent(
        targetState = searchWindowActive,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            fadeIn(tween(animationDuration)) togetherWith fadeOut(tween(animationDuration))
        },
        label = "searchWindow",
    ) { isSearchActive ->
        if (isSearchActive) {
            val searchResult by viewModel.trackSearchResultFlow.collectAsStateWithLifecycle()
            TrackSearchWindow(
                onSearch = { query -> viewModel.submitSearchKeyword(query) },
                onSearchClose = {
                    searchWindowActive = false
                    viewModel.resetSearch()
                },
                searchResult = searchResult,
                onTrackClick = onTrackClick
            )
//                DisposableEffect(Unit) { onDispose { searchWindowActive = false } }
        } else {
            val topBarBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
            ) {
                LibraryScreenTopBar(
                    modifier = Modifier.animateEnterExit(
                        enter = slideInVertically(
                            animationSpec = tween(animationDuration),
                            initialOffsetY = { -it / 2 }
                        ),
                        exit = slideOutVertically(
                            animationSpec = tween(animationDuration),
                            targetOffsetY = { -it / 2 }
                        )
                    ),
                    onSearchIconClick = { searchWindowActive = true },
                    onFilterIconClick = { filterSheetActive = !filterSheetActive },
                    isLibraryEmpty = isLibraryEmpty,
                    isFilterApplied = screenUiState.isFilterApplied(),
                    topAppBarScrollBehavior = topBarBehaviour
                )
                when (val uiState = screenUiState) {
                    LibraryUiState.Loading -> LoadingStub(
                        modifier = Modifier.fillMaxSize()
                    )

                    LibraryUiState.EmptyLibrary -> EmptyLibraryMessage(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    )

                    is LibraryUiState.Success -> {
                        val lazyGridState = rememberLazyGridState()
                        val filterSheetState = rememberModalBottomSheetState(
                            skipPartiallyExpanded = true
                        )
                        var newFilterApplied by rememberSaveable { mutableStateOf(false) }

                        fun hideFilterSheet(newTrackFilter: TrackFilter? = null) {
                            scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                                if (!filterSheetState.isVisible) filterSheetActive = false
                                newTrackFilter?.let {
                                    newFilterApplied = true
                                    viewModel.applyFilter(newTrackFilter)
                                }
                            }
                        }

                        LaunchedEffect(uiState.trackFilter) {
                            if (newFilterApplied) {
                                lazyGridState.animateScrollToItem(0)
                                newFilterApplied = false
                            }
                        }

                        TrackLazyGrid(
                            trackList = uiState.trackList,
                            onTrackClick = onTrackClick,
                            state = lazyGridState,
                            modifier = Modifier.nestedScroll(
                                topBarBehaviour.nestedScrollConnection
                            )
                        )
                        if (filterSheetActive) {
                            val filterState = rememberTrackFilterState(
                                initialTrackFilter = uiState.trackFilter
                            )
                            TrackFilterBottomSheet(
                                sheetState = filterSheetState,
                                filterState = filterState,
                                onDismissRequest = { hideFilterSheet() },
                                onApplyClick = {
                                    hideFilterSheet(filterState.makeFilter())
                                }
                            )
                        }

                    }
                }

            }
        }
    }
}

@Stable
private fun LibraryUiState.isFilterApplied(): Boolean {
    return when (this) {
        is LibraryUiState.Success -> !trackFilter.isEmpty
        else -> false
    }
}