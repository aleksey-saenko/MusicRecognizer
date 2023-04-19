package com.mrsep.musicrecognizer.feature.library.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import kotlinx.coroutines.launch
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR


private const val animationDuration = 300

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onTrackClick: (mbId: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiStateTransition = updateTransition(uiState, label = "LibraryUiState")
    val isLibraryEmpty = uiState !is LibraryUiState.Success
    var searchWindowActive by rememberSaveable { mutableStateOf(false) }
    var filterSheetActive by rememberSaveable { mutableStateOf(false) }

    AnimatedContent(
        targetState = searchWindowActive,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            fadeIn(tween(animationDuration)) with fadeOut(tween(animationDuration))
        },
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
            val topAppBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
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
                    isFilterApplied = isFilterApplied(uiState),
                    topAppBarScrollBehavior = topAppBarBehavior
                )
                uiStateTransition.AnimatedContent(
                    contentKey = { stateObject -> stateObject::class.simpleName },
                    transitionSpec = {
                        fadeIn(tween(animationDuration)) with fadeOut(tween(animationDuration))
                    },
                    modifier = Modifier.animateEnterExit(
                        enter = slideInVertically(
                            animationSpec = tween(animationDuration),
                            initialOffsetY = { it / 2 }
                        ),
                        exit = slideOutVertically(
                            animationSpec = tween(animationDuration),
                            targetOffsetY = { it / 2 }
                        )
                    ),
                ) { state ->
                    when (state) {
                        LibraryUiState.Loading -> LoadingStub()
                        LibraryUiState.EmptyLibrary -> EmptyLibraryMessage(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                        )
                        is LibraryUiState.Success -> {

                            val appliedFilter by viewModel.appliedFilterFlow.collectAsStateWithLifecycle()
                            val filterSheetState = rememberModalBottomSheetState(
                                skipPartiallyExpanded = true
                            )

                            fun hideFilterSheet(newTrackFilter: TrackFilter? = null) {
                                scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                                    if (!filterSheetState.isVisible) filterSheetActive = false
                                    newTrackFilter?.let(viewModel::applyFilter)
                                }
                            }

                            TrackLazyGrid(
                                trackList = state.trackList,
                                onTrackClick = onTrackClick,
                                modifier = Modifier.nestedScroll(
                                    topAppBarBehavior.nestedScrollConnection
                                )
                            )
                            if (filterSheetActive) {
                                val filterState = rememberTrackFilterState(
                                    initialTrackFilter = appliedFilter
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
}

private fun isFilterApplied(state: LibraryUiState): Boolean {
    return when (state) {
        is LibraryUiState.Success -> state.isFilterApplied
        else -> false
    }
}

@Composable
fun EmptyLibraryMessage(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(UiR.drawable.baseline_recently_24),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Text(
            text = stringResource(StringsR.string.empty_library_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(top = 24.dp)
        )
    }

}