package com.mrsep.musicrecognizer.feature.recognitionqueue.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.RemoteRecognitionResult
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun LazyColumnEnqueuedItem(
    enqueued: EnqueuedRecognition,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onDeleteEnqueued: (enqueuedId: Int) -> Unit,
    onRenameEnqueued: (enqueuedId: Int, name: String) -> Unit,
    onStartPlayRecord: (enqueuedId: Int) -> Unit,
    onStopPlayRecord: () -> Unit,
    onEnqueueRecognition: (enqueuedId: Int) -> Unit,
    onCancelRecognition: (enqueuedId: Int) -> Unit,
    onNavigateToTrackScreen: (trackMbId: String) -> Unit,
    menuEnabled: Boolean,
    selected: Boolean,
    onClick: (enqueuedId: Int) -> Unit,
    onLongClick: (enqueuedId: Int) -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                color = containerColor,
                shape = MaterialTheme.shapes.extraLarge
            )
            .combinedClickable(
                onLongClick = { onLongClick(enqueued.id) },
                onClick = { onClick(enqueued.id) },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
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
            this@Row.AnimatedVisibility(
                visible = menuEnabled,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessMedium))
            ) {
                IconButton(onClick = { menuExpanded = !menuExpanded }, enabled = menuEnabled) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null
                    )
                }
            }
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
                    }
                )
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
                    }
                )
                Divider()
                when (enqueued.status) {
                    EnqueuedRecognitionStatus.Inactive -> {
                        //FIXME: code duplicating
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
                                    painter = painterResource(UiR.drawable.baseline_cancel_schedule_send_24),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    is EnqueuedRecognitionStatus.Finished -> {
                        when (enqueued.status.remoteResult) {
                            RemoteRecognitionResult.NoMatches,
                            is RemoteRecognitionResult.Error -> {
                                //FIXME: code duplicating
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
                            is RemoteRecognitionResult.Success -> {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(StringsR.string.show_track),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    },
                                    onClick = {
                                        onNavigateToTrackScreen(
                                            enqueued.status.remoteResult.track.mbId
                                        )
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
            }
        }
        if (renameDialogVisible) {
            val title = getTitle(enqueued)
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

@Composable
private fun getTitle(enqueued: EnqueuedRecognition) =
    enqueued.title.ifBlank { stringResource(StringsR.string.untitled_recognition) }

@Composable
private fun getStatusMessage(status: EnqueuedRecognitionStatus): String {
    val appendix = when (status) {
        EnqueuedRecognitionStatus.Inactive -> stringResource(StringsR.string.idle)
        EnqueuedRecognitionStatus.Enqueued -> stringResource(StringsR.string.enqueued)
        EnqueuedRecognitionStatus.Running -> stringResource(StringsR.string.running)
        is EnqueuedRecognitionStatus.Finished -> when (status.remoteResult) {
            RemoteRecognitionResult.NoMatches -> stringResource(StringsR.string.no_matches_found)
            is RemoteRecognitionResult.Success -> stringResource(StringsR.string.track_found)
            is RemoteRecognitionResult.Error -> when (status.remoteResult) {
                RemoteRecognitionResult.Error.BadConnection -> stringResource(StringsR.string.bad_internet_connection)
                is RemoteRecognitionResult.Error.BadRecording -> stringResource(StringsR.string.recording_error)
                is RemoteRecognitionResult.Error.HttpError -> stringResource(StringsR.string.bad_network_response)
                is RemoteRecognitionResult.Error.UnhandledError -> stringResource(StringsR.string.internal_error)
                is RemoteRecognitionResult.Error.WrongToken -> if (status.remoteResult.isLimitReached)
                    stringResource(StringsR.string.token_limit_reached)
                else
                    stringResource(StringsR.string.wrong_token)
            }
        }
    }
    return stringResource(StringsR.string.status_colon, appendix)
}