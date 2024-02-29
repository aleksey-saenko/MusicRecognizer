package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduledJobStatus
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.EnqueuedRecognitionUi
import com.mrsep.musicrecognizer.feature.recognition.presentation.model.RemoteRecognitionResultUi
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun RecognitionLazyColumnItem(
    recognition: EnqueuedRecognitionUi,
    selected: Boolean,
    isPlaying: Boolean,
    onStartPlayRecord: () -> Unit,
    onStopPlayRecord: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCreationDate: Boolean = true,
    contentPadding: PaddingValues,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.background,
        label = "containerColor"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind { drawRect(color = containerColor) }
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick,
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(contentPadding)
    ) {
        Box(
            modifier = Modifier
                .heightIn(max = 100.dp)
                .aspectRatio(1f)
                .shadow(
                    elevation = 1.dp,
                    shape = shape
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    shape = shape
                )
                .clip(shape)
                .clickable {
                    if (isPlaying) onStopPlayRecord() else onStartPlayRecord()
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) +
                            scaleIn(
                                initialScale = 0f,
                                animationSpec = tween(220)
                            )).togetherWith(
                        fadeOut(animationSpec = tween(220)) +
                                scaleOut(
                                    targetScale = 0f,
                                    animationSpec = tween(220)
                                )
                    )
                },
                contentAlignment = Alignment.Center,
                label = "PlayerIcon"
            ) { playing ->
                if (playing) {
                    Icon(
                        painter = painterResource(UiR.drawable.rounded_pause_48),
                        contentDescription = stringResource(StringsR.string.stop_player),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(UiR.drawable.rounded_play_arrow_48),
                        contentDescription = stringResource(StringsR.string.start_player),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .heightIn(min = 100.dp)
                .padding(vertical = 2.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = recognition.getTitleMessage(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Text(
                text = recognition.getStatusMessage(false),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            if (showCreationDate) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = recognition.creationDateShort,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.End)
                        .alpha(0.9f)
                )
            }
        }
    }
}

@Composable
internal fun EnqueuedRecognitionUi.getTitleMessage(): String {
    return title.ifBlank { stringResource(StringsR.string.untitled_recognition) }
}

@Composable
internal fun EnqueuedRecognitionUi.getStatusMessage(concise: Boolean): String {
    val statusDescription = when (status) {
        ScheduledJobStatus.INACTIVE -> {
            when (result) {
                RemoteRecognitionResultUi.Error.BadConnection -> stringResource(StringsR.string.bad_internet_connection)
                is RemoteRecognitionResultUi.Error.BadRecording -> stringResource(StringsR.string.recording_error)
                is RemoteRecognitionResultUi.Error.HttpError -> stringResource(StringsR.string.bad_network_response)
                is RemoteRecognitionResultUi.Error.UnhandledError -> stringResource(StringsR.string.internal_error)
                is RemoteRecognitionResultUi.Error.AuthError -> stringResource(StringsR.string.auth_error)
                is RemoteRecognitionResultUi.Error.ApiUsageLimited -> stringResource(StringsR.string.service_usage_limited)
                RemoteRecognitionResultUi.NoMatches -> stringResource(StringsR.string.no_matches_found)
                is RemoteRecognitionResultUi.Success -> stringResource(StringsR.string.track_found)
                null -> stringResource(StringsR.string.idle)
            }
        }

        ScheduledJobStatus.ENQUEUED -> stringResource(StringsR.string.enqueued)
        ScheduledJobStatus.RUNNING -> stringResource(StringsR.string.running)
    }
    return if (concise) statusDescription else stringResource(
        StringsR.string.format_status,
        statusDescription
    )
}