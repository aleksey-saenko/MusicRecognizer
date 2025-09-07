package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
    val screenUiState by viewModel.screenUiStateFlow.collectAsStateWithLifecycle()

    when (val uiState = screenUiState) {
        QueueScreenUiState.Loading -> Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxSize()
        ) {
            QueueScreenLoadingTopBar(scrollBehavior = topBarBehaviour)
            LoadingStub(modifier = Modifier.fillMaxSize())
        }

        is QueueScreenUiState.Success -> {
            val context = LocalContext.current
            val playerStatus = uiState.playerStatus
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
            val multiSelectionState = rememberMultiSelectionState<Int>(uiState.recognitionList)
            BackHandler(
                enabled = multiSelectionState.hasSelected,
                onBack = multiSelectionState::deselectAll
            )

            var deleteDialogVisible by rememberSaveable(uiState.recognitionList) {
                mutableStateOf(false)
            }
            var deletionInProgress by rememberSaveable(uiState.recognitionList) {
                mutableStateOf(false)
            }
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QueueScreenTopBar(
                    isQueueEmpty = uiState.recognitionList.isEmpty(),
                    isMultiselectEnabled = multiSelectionState.hasSelected,
                    selectedCount = multiSelectionState.selectedCount,
                    totalCount = uiState.recognitionList.size,
                    onSelectAll = {
                        multiSelectionState.select(uiState.recognitionList.map { it.id })
                    },
                    onDeselectAll = multiSelectionState::deselectAll,
                    onCancelSelected = {
                        viewModel.cancelRecognitions(multiSelectionState.getSelected())
                    },
                    onDeleteSelected = { deleteDialogVisible = true },
                    useGridLayout = uiState.useGridLayout,
                    onChangeUseGridLayout = viewModel::setUseGridLayout,
                    showCreationDate = uiState.showCreationDate,
                    onChangeShowCreationDate = viewModel::setShowCreationDate,
                    scrollBehavior = topBarBehaviour,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = uiState.useGridLayout,
                        label = "Layout"
                    ) { useGrid ->
                        if (useGrid) {
                            RecognitionLazyGrid(
                                lazyGridState = rememberLazyGridState(),
                                recognitionList = uiState.recognitionList,
                                showCreationDate = uiState.showCreationDate,
                                multiSelectionState = multiSelectionState,
                                onNavigateToTrackScreen = onNavigateToTrackScreen,
                                onEnqueueRecognition = viewModel::enqueueRecognition,
                                onCancelRecognition = viewModel::cancelRecognition,
                                onDeleteEnqueued = viewModel::cancelAndDeleteRecognition,
                                onRenameEnqueued = viewModel::renameRecognition,
                                playerStatus = uiState.playerStatus,
                                onStartPlayRecord = viewModel::startAudioPlayer,
                                onStopPlayRecord = viewModel::stopAudioPlayer,
                                modifier = Modifier
                                    .nestedScroll(topBarBehaviour.nestedScrollConnection)
                                    .fillMaxSize()
                            )
                        } else {
                            RecognitionLazyColumn(
                                lazyListState = rememberLazyListState(),
                                recognitionList = uiState.recognitionList,
                                showCreationDate = uiState.showCreationDate,
                                multiSelectionState = multiSelectionState,
                                onNavigateToTrackScreen = onNavigateToTrackScreen,
                                onEnqueueRecognition = viewModel::enqueueRecognition,
                                onCancelRecognition = viewModel::cancelRecognition,
                                onDeleteEnqueued = viewModel::cancelAndDeleteRecognition,
                                onRenameEnqueued = viewModel::renameRecognition,
                                playerStatus = uiState.playerStatus,
                                onStartPlayRecord = viewModel::startAudioPlayer,
                                onStopPlayRecord = viewModel::stopAudioPlayer,
                                modifier = Modifier
                                    .nestedScroll(topBarBehaviour.nestedScrollConnection)
                                    .fillMaxSize()
                            )
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.recognitionList.isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EmptyQueueMessage(
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.surface)
                                .fillMaxSize()
                        )
                    }
                }
            }
            if (deleteDialogVisible) {
                DeleteSelectedDialog(
                    onDeleteClick = {
                        deletionInProgress = true
                        viewModel.cancelAndDeleteRecognitions(multiSelectionState.getSelected())
                    },
                    onDismissClick = { deleteDialogVisible = false },
                    inProgress = deletionInProgress
                )
            }
        }
    }
}
