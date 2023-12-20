package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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
    onNavigateToTrackScreen: (trackId: String) -> Unit
) {
    val topBarBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val screenState by viewModel.screenUiStateFlow.collectAsStateWithLifecycle()

    when (val state = screenState) {
        QueueScreenUiState.Loading -> LoadingStub(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        )

        is QueueScreenUiState.Success -> {
            val context = LocalContext.current
            val playerStatus = state.playerStatus
            LaunchedEffect(playerStatus) {
                if (playerStatus is PlayerStatus.Error) {
                    Toast.makeText(context, playerStatus.message, Toast.LENGTH_LONG).show()
                }
            }
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
                    .background(color = MaterialTheme.colorScheme.background)
                    .systemBarsPadding(),
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
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.nestedScroll(topBarBehaviour.nestedScrollConnection)
                    ) {
                        items(
                            items = state.enqueuedList,
                            key = { enqueuedWithStatus -> enqueuedWithStatus.enqueued.id }
                        ) { enqueuedWithStatus ->
                            LazyColumnEnqueuedItem(
                                enqueuedWithStatus = enqueuedWithStatus,
                                isPlaying = enqueuedWithStatus.enqueued.isPlaying(
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
                                    enqueuedWithStatus.enqueued.id
                                ),
                                onClick = { recognitionId ->
                                    if (multiSelectionState.multiselectEnabled) {
                                        multiSelectionState.toggleSelection(recognitionId)
                                    }
                                },
                                onLongClick = multiSelectionState::toggleSelection,
                                modifier = Modifier.animateItemPlacement()
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

