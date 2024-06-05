package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.MultiSelectionState
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.EnqueuedRecognitionUi
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.PlayerStatusUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun RecognitionLazyGrid(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState,
    showCreationDate: Boolean,
    multiSelectionState: MultiSelectionState<Int>,
    recognitionList: ImmutableList<EnqueuedRecognitionUi>,
    onNavigateToTrackScreen: (trackId: String) -> Unit,
    onEnqueueRecognition: (recognitionId: Int, forceLaunch: Boolean) -> Unit,
    onCancelRecognition: (recognitionId: Int) -> Unit,
    onDeleteEnqueued: (recognitionId: Int) -> Unit,
    onRenameEnqueued: (recognitionId: Int, newTitle: String) -> Unit,
    playerStatus: PlayerStatusUi,
    onStartPlayRecord: (recognitionId: Int) -> Unit,
    onStopPlayRecord: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 108.dp),
        state = lazyGridState,
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(
            items = recognitionList,
            key = { recognition -> recognition.id }
        ) { recognition ->
            val scope = rememberCoroutineScope()
            var showActionsSheet by rememberSaveable { mutableStateOf(false) }
            var showRenameDialog by rememberSaveable(recognition.title) {
                mutableStateOf(false)
            }
            RecognitionLazyGridItem(
                recognition = recognition,
                isPlaying = recognition.isPlaying(playerStatus),
                selected = multiSelectionState.isSelected(recognition.id),
                onStartPlayRecord = { onStartPlayRecord(recognition.id) },
                onStopPlayRecord = onStopPlayRecord,
                onClick = {
                    if (multiSelectionState.multiselectEnabled) {
                        multiSelectionState.toggleSelection(recognition.id)
                    } else {
                        showActionsSheet = !showActionsSheet
                    }
                },
                onLongClick = { multiSelectionState.toggleSelection(recognition.id) },
                showCreationDate = showCreationDate,
                modifier = Modifier.animateItemPlacement(),
                contentPadding = PaddingValues(4.dp)
            )
            val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            fun hideActionsSheet(onHidden: () -> Unit = {}) {
                scope.launch { actionsSheetState.hide() }.invokeOnCompletion {
                    if (!actionsSheetState.isVisible) {
                        showActionsSheet = false
                        onHidden()
                    }
                }
            }
            if (showActionsSheet) {
                RecognitionActionsBottomSheet(
                    sheetState = actionsSheetState,
                    recognition = recognition,
                    onDismissRequest = ::hideActionsSheet,
                    onDeleteEnqueued = {
                        hideActionsSheet { onDeleteEnqueued(recognition.id) }
                    },
                    onRenameEnqueued = { showRenameDialog = true },
                    onEnqueueRecognition = { forceLaunch ->
                        onEnqueueRecognition(recognition.id, forceLaunch)
                    },
                    onCancelRecognition = { onCancelRecognition(recognition.id) },
                    onNavigateToTrackScreen = { trackId ->
                        hideActionsSheet { onNavigateToTrackScreen(trackId) }
                    },
                )
            }
            if (showRenameDialog) {
                RenameRecognitionDialog(
                    initialName = recognition.getTitleMessage(),
                    onConfirmClick = { newTitle ->
                        onRenameEnqueued(recognition.id, newTitle)
                    },
                    onDismissClick = {
                        showRenameDialog = false
                    }
                )
            }
        }
    }
}
