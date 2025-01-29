package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduledJobStatus
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.EnqueuedRecognitionUi
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.RemoteRecognitionResultUi
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecognitionActionsBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    recognition: EnqueuedRecognitionUi,
    onEnqueueRecognition: (forceLaunch: Boolean) -> Unit,
    onCancelRecognition: () -> Unit,
    onNavigateToTrackScreen: (trackId: String) -> Unit,
    onRenameEnqueued: () -> Unit,
    onDeleteEnqueued: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = recognition.getTitleMessage(),
            label = "Title",
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(220, delayMillis = 90)
                        ))
                    .togetherWith(
                        fadeOut(animationSpec = tween(90)) + slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(90)
                        )
                    )
            },
            contentAlignment = Alignment.CenterStart
        ) { titleMessage ->
            Text(
                text = titleMessage,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(
                StringsR.string.format_recognition_created,
                recognition.creationDateLong
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        AnimatedContent(
            targetState = recognition.getStatusMessage(false),
            label = "Status",
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(220, delayMillis = 90)
                        ))
                    .togetherWith(
                        fadeOut(animationSpec = tween(90)) + slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(90)
                        )
                    )
            },
            contentAlignment = Alignment.CenterStart
        ) { status ->
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .weight(1f, false)
        ) {
            AnimatedContent(
                targetState = recognition.status,
                label = "Actions",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(220, delayMillis = 90)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(90)) + slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(90)
                            )
                        )
                },
                contentAlignment = Alignment.CenterStart
            ) { status ->
                when (status) {
                    ScheduledJobStatus.INACTIVE -> {
                        when (recognition.result) {
                            is RemoteRecognitionResultUi.Success -> {
                                BottomSheetAction(
                                    title = stringResource(R.string.recognition_action_show_track),
                                    iconResId = UiR.drawable.outline_audio_file_24,
                                    onClick = {
                                        onNavigateToTrackScreen(recognition.result.track.id)
                                    }
                                )
                            }

                            RemoteRecognitionResultUi.NoMatches,
                            is RemoteRecognitionResultUi.Error,
                            null -> {
                                Column {
                                    BottomSheetAction(
                                        title = stringResource(StringsR.string.recognition_action_enqueue),
                                        iconResId = UiR.drawable.outline_schedule_send_24,
                                        onClick = { onEnqueueRecognition(false) }
                                    )
                                    BottomSheetAction(
                                        title = stringResource(StringsR.string.recognition_action_force_launch),
                                        iconResId = UiR.drawable.outline_send_24,
                                        onClick = { onEnqueueRecognition(true) }
                                    )
                                }
                            }
                        }
                    }

                    ScheduledJobStatus.ENQUEUED,
                    ScheduledJobStatus.RUNNING -> {
                        BottomSheetAction(
                            title = stringResource(R.string.action_cancel_recognition),
                            iconResId = UiR.drawable.outline_cancel_schedule_send_24,
                            onClick = onCancelRecognition
                        )
                    }
                }
            }
            BottomSheetAction(
                title = stringResource(StringsR.string.recognition_action_rename),
                iconResId = UiR.drawable.outline_edit_24,
                onClick = onRenameEnqueued
            )
            BottomSheetAction(
                title = stringResource(StringsR.string.delete),
                iconResId = UiR.drawable.outline_delete_24,
                onClick = onDeleteEnqueued
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BottomSheetAction(
    title: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(4.dp))
        Icon(
            painter = painterResource(iconResId),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
