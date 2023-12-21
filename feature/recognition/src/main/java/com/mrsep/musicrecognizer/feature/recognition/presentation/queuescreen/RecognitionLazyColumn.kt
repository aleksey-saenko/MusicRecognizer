package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.MultiSelectionState
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognitionWithStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.PlayerStatus
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun RecognitionLazyColumn(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    multiSelectionState: MultiSelectionState<Int>,
    recognitionList: ImmutableList<EnqueuedRecognitionWithStatus>,
    onNavigateToTrackScreen: (trackId: String) -> Unit,
    onEnqueueRecognition: (recognitionId: Int, forceLaunch: Boolean) -> Unit,
    onCancelRecognition: (recognitionId: Int) -> Unit,
    onDeleteEnqueued: (recognitionId: Int) -> Unit,
    onRenameEnqueued: (recognitionId: Int, newTitle: String) -> Unit,
    playerStatus: PlayerStatus,
    onStartPlayRecord: (recognitionId: Int) -> Unit,
    onStopPlayRecord: () -> Unit,
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(
            items = recognitionList,
            key = { enqueuedWithStatus -> enqueuedWithStatus.enqueued.id }
        ) { enqueuedWithStatus ->
            val recognitionId = enqueuedWithStatus.enqueued.id
            LazyColumnEnqueuedItem(
                enqueuedWithStatus = enqueuedWithStatus,
                isPlaying = enqueuedWithStatus.enqueued.isPlaying(playerStatus),
                menuEnabled = !multiSelectionState.multiselectEnabled,
                selected = multiSelectionState.isSelected(recognitionId),
                onDeleteEnqueued = { onDeleteEnqueued(recognitionId) },
                onRenameEnqueued = { newTitle -> onRenameEnqueued(recognitionId, newTitle) },
                onStartPlayRecord = { onStartPlayRecord(recognitionId) },
                onStopPlayRecord = onStopPlayRecord,
                onEnqueueRecognition = { forceLaunch ->
                    onEnqueueRecognition(recognitionId, forceLaunch)
                },
                onCancelRecognition = { onCancelRecognition(recognitionId) },
                onNavigateToTrackScreen = onNavigateToTrackScreen,
                onClick = {
                    if (multiSelectionState.multiselectEnabled) {
                        multiSelectionState.toggleSelection(recognitionId)
                    }
                },
                onLongClick = { multiSelectionState.toggleSelection(recognitionId) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

private fun EnqueuedRecognition.isPlaying(playerStatus: PlayerStatus) =
    playerStatus is PlayerStatus.Started && playerStatus.record == this.recordFile