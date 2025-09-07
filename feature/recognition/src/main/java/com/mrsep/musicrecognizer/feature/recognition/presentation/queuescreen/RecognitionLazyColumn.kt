package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.MultiSelectionState
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.EnqueuedRecognitionUi
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.PlayerStatusUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecognitionLazyColumn(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
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
    onStopPlayRecord: () -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        itemsIndexed(
            items = recognitionList,
            key = { _, recognition -> recognition.id }
        ) { index, recognition ->
            val scope = rememberCoroutineScope()
            var showActionsSheet by rememberSaveable { mutableStateOf(false) }
            var showRenameDialog by rememberSaveable(recognition.title) {
                mutableStateOf(false)
            }
            Column(modifier = Modifier.animateItem()) {
                RecognitionLazyColumnItem(
                    recognition = recognition,
                    isPlaying = recognition.isPlaying(playerStatus),
                    selected = multiSelectionState.isSelected(recognition.id),
                    onStartPlayRecord = { onStartPlayRecord(recognition.id) },
                    onStopPlayRecord = onStopPlayRecord,
                    onClick = {
                        if (multiSelectionState.hasSelected) {
                            multiSelectionState.toggleSelection(recognition.id)
                        } else {
                            showActionsSheet = !showActionsSheet
                        }
                    },
                    onLongClick = { multiSelectionState.toggleSelection(recognition.id) },
                    showCreationDate = showCreationDate,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                )
                if (index != recognitionList.lastIndex) {
                    HorizontalDivider(modifier = Modifier.alpha(0.2f))
                }
            }

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
                    onDismissRequest = { showActionsSheet = false },
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

internal fun EnqueuedRecognitionUi.isPlaying(playerStatus: PlayerStatusUi) =
    playerStatus is PlayerStatusUi.Started && playerStatus.id == this.id
