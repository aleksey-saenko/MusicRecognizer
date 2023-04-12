package com.mrsep.musicrecognizer.feature.recognitionqueue.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionWithStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun QueueScreen(
    modifier: Modifier = Modifier,
    viewModel: QueueScreenViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onNavigateToTrackScreen: (trackMbId: String) -> Unit
) {
    val enqueuedWithStatusList by viewModel.enqueuedRecognitionUiFlow.collectAsStateWithLifecycle()
    val playerStatus by viewModel.playerStatusFlow.collectAsStateWithLifecycle(PlayerStatus.Idle)
    val topBarBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QueueScreenTopBar(
            topAppBarScrollBehavior = topBarBehaviour,
            onBackPressed = onBackPressed
        )
        LazyColumn(
            modifier = Modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = enqueuedWithStatusList.size,
                key = { index -> enqueuedWithStatusList[index].id }
            ) { index ->
                LazyColumnEnqueuedItem(
                    enqueued = enqueuedWithStatusList[index],
                    isPlaying = enqueuedWithStatusList[index].isPlaying(playerStatus),
                    onDeleteEnqueued = { enqueuedId ->
                        viewModel.deleteEnqueuedRecognition(enqueuedId)
                    },
                    onRenameEnqueued = { enqueuedId, newName ->
                        viewModel.renameEnqueuedRecognition(enqueuedId, newName)
                    },
                    onStartPlayRecord = { enqueuedId ->
                        viewModel.startPlayRecord(enqueuedId)
                    },
                    onStopPlayRecord = {
                        viewModel.stopPlayer()
                    },
                    onEnqueueRecognition = { enqueuedId ->
                        viewModel.enqueueRecognition(enqueuedId)
                    },
                    onCancelRecognition = { enqueuedId ->
                        viewModel.cancelRecognition(enqueuedId)
                    },
                    onNavigateToTrackScreen = onNavigateToTrackScreen,
                    modifier = Modifier.animateItemPlacement(
                        tween(300)
                    )
                )
            }
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LazyColumnEnqueuedItem(
    enqueued: EnqueuedRecognitionWithStatus,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onDeleteEnqueued: (enqueuedId: Int) -> Unit,
    onRenameEnqueued: (enqueuedId: Int, name: String) -> Unit,
    onStartPlayRecord: (enqueuedId: Int) -> Unit,
    onStopPlayRecord: () -> Unit,
    onEnqueueRecognition: (enqueuedId: Int) -> Unit,
    onCancelRecognition: (enqueuedId: Int) -> Unit,
    onNavigateToTrackScreen: (trackMbId: String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            shape = CircleShape,
            onClick = {
                if (isPlaying) {
                    onStopPlayRecord()
                } else {
                    onStartPlayRecord(enqueued.id)
                }
            },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(48.dp)
        ) {
            AnimatedContent(
                targetState = isPlaying,
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) +
                            scaleIn(
                                initialScale = 0f,
                                animationSpec = tween(220)
                            ) with
                            fadeOut(animationSpec = tween(220)) +
                            scaleOut(
                                targetScale = 0f,
                                animationSpec = tween(220)
                            )
                }
            ) {
                if (it) {
                    Icon(
                        painter = painterResource(UiR.drawable.baseline_pause_24),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = getTitle(enqueued)
            )
            Text(
                text = getStatusMessage(enqueued.status),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Created: " + enqueued.creationDate.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        var menuExpanded by remember { mutableStateOf(false) }
        var renameDialogVisible by remember { mutableStateOf(false) }
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { menuExpanded = !menuExpanded }
                    .padding(4.dp)
                    .size(24.dp)
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(StringsR.string.rename)) },
                    onClick = {
                        renameDialogVisible = true
                        menuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null
                        )
                    })
                DropdownMenuItem(
                    text = { Text(text = stringResource(StringsR.string.delete)) },
                    onClick = {
                        onDeleteEnqueued(enqueued.id)
                        menuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null
                        )
                    })
                Divider()
                when (enqueued.status) {
                    is EnqueuedRecognitionStatus.Finished.Error,
                    EnqueuedRecognitionStatus.Finished.NotFound,
                    EnqueuedRecognitionStatus.Canceled,
                    EnqueuedRecognitionStatus.Inactive -> {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(StringsR.string.enqueue_recognition),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            onClick = {
                                onEnqueueRecognition(enqueued.id)
                                menuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    EnqueuedRecognitionStatus.Enqueued,
                    EnqueuedRecognitionStatus.Running -> {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(StringsR.string.cancel_recognition),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            onClick = {
                                onCancelRecognition(enqueued.id)
                                menuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    is EnqueuedRecognitionStatus.Finished.Success -> {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(StringsR.string.show_track),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            onClick = {
                                onNavigateToTrackScreen(enqueued.status.trackMbId)
                                menuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(UiR.drawable.baseline_audio_file_24),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
        if (renameDialogVisible) {
            val title = getTitle(enqueued) //FIXME too many recompositions
            var newName by rememberSaveable { mutableStateOf(title) }
            AlertDialog(
                onDismissRequest = { renameDialogVisible = false },
                confirmButton = {
                    TextButton(onClick = {
                        onRenameEnqueued(enqueued.id, newName)
                        renameDialogVisible = false
                    }) {
                        Text(text = stringResource(StringsR.string.save))
                    }
                },
                modifier = Modifier,
                dismissButton = {
                    TextButton(onClick = { renameDialogVisible = false }) {
                        Text(text = stringResource(StringsR.string.cancel))
                    }
                },
                icon = {

                },
                title = {
                    Text(
                        text = stringResource(StringsR.string.rename),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it }
                    )
                },
            )
        }


    }

}

private fun EnqueuedRecognitionWithStatus.isPlaying(playerStatus: PlayerStatus) =
    playerStatus is PlayerStatus.Started && playerStatus.record == this.recordFile

@Composable
private fun getTitle(enqueued: EnqueuedRecognitionWithStatus) =
    enqueued.title.ifBlank { stringResource(StringsR.string.untitled_recognition) }

@Composable
private fun getStatusMessage(status: EnqueuedRecognitionStatus): String {
    return when (status) {
        EnqueuedRecognitionStatus.Canceled -> "Status: Canceled"
        EnqueuedRecognitionStatus.Enqueued -> "Status: Enqueued"
        is EnqueuedRecognitionStatus.Finished.Error -> "Status: Failed (${status.message})"
        EnqueuedRecognitionStatus.Finished.NotFound -> "Status: No matches found"
        is EnqueuedRecognitionStatus.Finished.Success -> "Status: Track found"
        EnqueuedRecognitionStatus.Inactive -> "Status: Inactive"
        EnqueuedRecognitionStatus.Running -> "Status: Running"
    }
}

