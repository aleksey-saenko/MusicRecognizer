package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.components.rememberMultiSelectionState
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.PlayerStatusUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QueueScreen(
    viewModel: QueueScreenViewModel = hiltViewModel(),
    onNavigateToTrackScreen: (trackId: String) -> Unit
) {
    val topBarBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    val screenState by viewModel.screenUiStateFlow.collectAsStateWithLifecycle()

    when (val state = screenState) {
        QueueScreenUiState.Loading -> LoadingStub(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .systemBarsPadding()
        )

        is QueueScreenUiState.Success -> {
            val context = LocalContext.current
            val playerStatus = state.playerStatus
            LaunchedEffect(playerStatus) {
                if (playerStatus is PlayerStatusUi.Error) {
                    Toast.makeText(context, playerStatus.message, Toast.LENGTH_LONG).show()
                }
            }
            LifecycleStartEffect(viewModel) {
                onStopOrDispose {
                    viewModel.stopAudioPlayer()
                }
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
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QueueScreenTopBar(
                    scrollBehavior = topBarBehaviour,
                    multiselectEnabled = multiSelectionState.multiselectEnabled,
                    selectedCount = multiSelectionState.selectedCount,
                    totalCount = state.enqueuedList.size,
                    onSelectAll = {
                        multiSelectionState.select(state.enqueuedList.map { it.id })
                    },
                    onDeselectAll = multiSelectionState::deselectAll,
                    onCancelSelected = {
                        viewModel.cancelRecognition(*multiSelectionState.getSelected().toIntArray())
                    },
                    onDeleteSelected = { deleteDialogVisible = true },
                    onDisableSelectionMode = multiSelectionState::deselectAll
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.useGridLayout) {
                        RecognitionLazyGrid(
                            lazyGridState = rememberLazyGridState(),
                            multiSelectionState = multiSelectionState,
                            recognitionList = state.enqueuedList,
                            onNavigateToTrackScreen = onNavigateToTrackScreen,
                            onEnqueueRecognition = viewModel::enqueueRecognition,
                            onCancelRecognition = viewModel::cancelRecognition,
                            onDeleteEnqueued = viewModel::cancelAndDeleteRecognition,
                            onRenameEnqueued = viewModel::renameRecognition,
                            playerStatus = state.playerStatus,
                            onStartPlayRecord = viewModel::startAudioPlayer,
                            onStopPlayRecord = viewModel::stopAudioPlayer,
                            modifier = Modifier
                                .nestedScroll(topBarBehaviour.nestedScrollConnection)
                                .fillMaxSize()
                        )
                    } else {
                        RecognitionLazyColumn(
                            lazyListState = rememberLazyListState(),
                            multiSelectionState = multiSelectionState,
                            recognitionList = state.enqueuedList,
                            onNavigateToTrackScreen = onNavigateToTrackScreen,
                            onEnqueueRecognition = viewModel::enqueueRecognition,
                            onCancelRecognition = viewModel::cancelRecognition,
                            onDeleteEnqueued = viewModel::cancelAndDeleteRecognition,
                            onRenameEnqueued = viewModel::renameRecognition,
                            playerStatus = state.playerStatus,
                            onStartPlayRecord = viewModel::startAudioPlayer,
                            onStopPlayRecord = viewModel::stopAudioPlayer,
                            modifier = Modifier
                                .nestedScroll(topBarBehaviour.nestedScrollConnection)
                                .fillMaxSize()
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.enqueuedList.isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EmptyQueueMessage(Modifier.fillMaxSize())
                    }
                }
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
        }
    }

}