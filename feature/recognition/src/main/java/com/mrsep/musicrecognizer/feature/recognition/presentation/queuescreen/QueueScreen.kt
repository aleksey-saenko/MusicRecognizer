package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.components.rememberMultiSelectionState
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognition.domain.model.PlayerStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun QueueScreen(
    viewModel: QueueScreenViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onNavigateToTrackScreen: (trackMbId: String) -> Unit
) {
    val topBarBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val screenState by viewModel.screenUiStateFlow.collectAsStateWithLifecycle()

    when (val state = screenState) {
        QueueScreenUiState.Loading -> LoadingStub(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        )

        is QueueScreenUiState.Success -> {

            val lifecycle = LocalLifecycleOwner.current.lifecycle
            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) viewModel.stopAudioPlayer()
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            val multiSelectionState = rememberMultiSelectionState<Int>(state.enqueuedList)
            BackHandler(
                enabled = multiSelectionState.multiselectEnabled,
                onBack = multiSelectionState::deselectAll
            )

            var deleteDialogVisible by rememberSaveable(state.enqueuedList) {
                mutableStateOf(false)
            }
            var deletionInProgress by rememberSaveable(state.enqueuedList) {
                mutableStateOf(false)
            }
            if (deleteDialogVisible) {
                DeleteSelectedDialog(
                    onDeleteClick = {
                        deletionInProgress = true
                        viewModel.cancelAndDeleteRecognition(
                            *multiSelectionState.getSelected().toIntArray()
                        )
                    },
                    onDismissClick = { deleteDialogVisible = false },
                    inProgress = deletionInProgress
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QueueScreenTopBar(
                    scrollBehavior = topBarBehaviour,
                    onBackPressed = onBackPressed,
                    multiselectEnabled = multiSelectionState.multiselectEnabled,
                    selectedCount = multiSelectionState.selectedCount,
                    totalCount = state.enqueuedList.size,
                    onSelectAll = {
                        multiSelectionState.select(state.enqueuedList.map { it.enqueued.id })
                    },
                    onDeselectAll = multiSelectionState::deselectAll,
                    onCancelSelected = {
                        viewModel.cancelRecognition(*multiSelectionState.getSelected().toIntArray())
                    },
                    onDeleteSelected = { deleteDialogVisible = true },
                    onDisableSelectionMode = multiSelectionState::deselectAll
                )
                if (state.enqueuedList.isEmpty()) {
                    EmptyQueueMessage(modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.nestedScroll(topBarBehaviour.nestedScrollConnection)
                    ) {
                        items(
                            count = state.enqueuedList.size,
                            key = { index -> state.enqueuedList[index].enqueued.id }
                        ) { index ->
                            LazyColumnEnqueuedItem(
                                enqueuedWithStatus = state.enqueuedList[index],
                                isPlaying = state.enqueuedList[index].enqueued.isPlaying(
                                    state.playerStatus
                                ),
                                onDeleteEnqueued = viewModel::cancelAndDeleteRecognition,
                                onRenameEnqueued = viewModel::renameRecognition,
                                onStartPlayRecord = viewModel::startAudioPlayer,
                                onStopPlayRecord = viewModel::stopAudioPlayer,
                                onEnqueueRecognition = viewModel::enqueueRecognition,
                                onCancelRecognition = viewModel::cancelRecognition,
                                onNavigateToTrackScreen = onNavigateToTrackScreen,
                                menuEnabled = !multiSelectionState.multiselectEnabled,
                                selected = multiSelectionState.isSelected(
                                    state.enqueuedList[index].enqueued.id
                                ),
                                onClick = { enqueuedId ->
                                    if (multiSelectionState.multiselectEnabled) {
                                        multiSelectionState.toggleSelection(enqueuedId)
                                    }
                                },
                                onLongClick = multiSelectionState::toggleSelection,
                                modifier = Modifier.animateItemPlacement(
                                    tween(300)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

}

private fun EnqueuedRecognition.isPlaying(playerStatus: PlayerStatus) =
    playerStatus is PlayerStatus.Started && playerStatus.record == this.recordFile

