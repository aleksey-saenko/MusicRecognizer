package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.mrsep.musicrecognizer.core.ui.components.rememberMultiSelectionState
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(
    onTrackClick: (trackId: String) -> Unit,
    onTrackSearchClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val screenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLibraryEmpty = screenUiState !is LibraryUiState.Success
    var filterSheetActive by rememberSaveable { mutableStateOf(false) }
    val multiSelectionState = rememberMultiSelectionState<String>(screenUiState)
    var deleteDialogVisible by rememberSaveable(screenUiState) { mutableStateOf(false) }
    var deletionInProgress by rememberSaveable(screenUiState) { mutableStateOf(false) }

    BackHandler(
        enabled = multiSelectionState.multiselectEnabled,
        onBack = multiSelectionState::deselectAll
    )

    val topBarBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) {
        LibraryScreenTopBar(
            isLibraryEmpty = isLibraryEmpty,
            isFilterApplied = screenUiState.isFilterApplied(),
            isMultiselectEnabled = multiSelectionState.multiselectEnabled,
            selectedCount = multiSelectionState.selectedCount,
            totalCount = screenUiState.getTrackCount(),
            onSearchIconClick = onTrackSearchClick,
            onFilterIconClick = { filterSheetActive = !filterSheetActive },
            onDeleteIconClick = { deleteDialogVisible = true },
            onSelectAll = { multiSelectionState.select(screenUiState.getTrackIdList()) },
            onDeselectAll = multiSelectionState::deselectAll,
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

            is LibraryUiState.Success -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                var newFilterApplied by rememberSaveable { mutableStateOf(false) }

                if (uiState.useColumnLayout) {
                    val lazyListState = rememberLazyListState()
                    TrackLazyColumn(
                        trackList = uiState.trackList,
                        onTrackClick = onTrackClick,
                        lazyListState = lazyListState,
                        multiSelectionState = multiSelectionState,
                        modifier = Modifier
                            .nestedScroll(topBarBehaviour.nestedScrollConnection)
                            .fillMaxSize()
                    )
                    LaunchedEffect(uiState.trackFilter) {
                        if (newFilterApplied) {
                            lazyListState.animateScrollToItem(0)
                            newFilterApplied = false
                        }
                    }
                } else {
                    val lazyGridState = rememberLazyGridState()
                    TrackLazyGrid(
                        trackList = uiState.trackList,
                        onTrackClick = onTrackClick,
                        lazyGridState = lazyGridState,
                        multiSelectionState = multiSelectionState,
                        modifier = Modifier
                            .nestedScroll(topBarBehaviour.nestedScrollConnection)
                            .fillMaxSize()
                    )
                    LaunchedEffect(uiState.trackFilter) {
                        if (newFilterApplied) {
                            lazyGridState.animateScrollToItem(0)
                            newFilterApplied = false
                        }
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.trackList.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    NoFilteredTracksMessage(
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.background)
                            .fillMaxSize()
                    )
                }

                val filterSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
                fun hideFilterSheet(newTrackFilter: TrackFilter? = null) {
                    scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                        if (!filterSheetState.isVisible) filterSheetActive = false
                        newTrackFilter?.let {
                            newFilterApplied = true
                            viewModel.applyFilter(newTrackFilter)
                        }
                    }
                }
                if (filterSheetActive) {
                    val filterState = rememberTrackFilterState(
                        initialTrackFilter = uiState.trackFilter
                    )
                    TrackFilterBottomSheet(
                        sheetState = filterSheetState,
                        filterState = filterState,
                        onDismissRequest = ::hideFilterSheet,
                        onApplyClick = {
                            val newFilter = filterState.makeFilter()
                            hideFilterSheet(newFilter)
                        }
                    )
                }

                if (deleteDialogVisible) {
                    DeleteSelectedDialog(
                        onDeleteClick = {
                            deletionInProgress = true
                            val selectedIds = multiSelectionState.getSelected()
                            viewModel.deleteTracks(selectedIds)
                        },
                        onDismissClick = { deleteDialogVisible = false },
                        inProgress = deletionInProgress
                    )
                }
            }
        }
    }
}

@Stable
private fun LibraryUiState.isFilterApplied(): Boolean {
    return when (this) {
        is LibraryUiState.Success -> !trackFilter.isDefault
        else -> false
    }
}

@Stable
private fun LibraryUiState.getTrackCount(): Int {
    return when (this) {
        is LibraryUiState.Success -> trackList.size
        else -> 0
    }
}

@Stable
private fun LibraryUiState.getTrackIdList(): List<String> {
    return when (this) {
        is LibraryUiState.Success -> trackList.map { it.id }
        else -> emptyList()
    }
}