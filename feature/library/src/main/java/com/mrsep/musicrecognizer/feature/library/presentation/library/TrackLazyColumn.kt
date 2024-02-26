package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.core.ui.components.MultiSelectionState
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import kotlinx.collections.immutable.ImmutableList
import com.mrsep.musicrecognizer.core.ui.R as UiR

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackLazyColumn(
    trackList: ImmutableList<TrackUi>,
    onTrackClick: (trackId: String) -> Unit,
    lazyListState: LazyListState,
    multiSelectionState: MultiSelectionState<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
    ) {
        itemsIndexed(
            items = trackList,
            key = { _, track -> track.id }
        ) { index, track ->
            Column(modifier = Modifier.animateItemPlacement()) {
                TrackLazyColumnItem(
                    track = track,
                    selected = multiSelectionState.isSelected(track.id),
                    onClick = {
                        if (multiSelectionState.multiselectEnabled) {
                            multiSelectionState.toggleSelection(track.id)
                        } else {
                            onTrackClick(track.id)
                        }
                    },
                    onLongClick = { multiSelectionState.toggleSelection(track.id) },
                    contentPadding = PaddingValues(vertical = 5.dp, horizontal = 12.dp)
                )
                if (index != trackList.lastIndex) {
                    HorizontalDivider(modifier = Modifier.alpha(0.2f))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackLazyColumnItem(
    track: TrackUi,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    showRecognitionDate: Boolean = true,
    contentPadding: PaddingValues
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.background,
        label = "containerColor"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
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
        val placeholder = forwardingPainter(
            painter = painterResource(UiR.drawable.outline_album_fill1_24),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            alpha = 0.3f
        )
        AsyncImage(
            model = track.artworkUrl,
            fallback = placeholder,
            error = placeholder,
            contentDescription = stringResource(com.mrsep.musicrecognizer.core.strings.R.string.artwork),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .shadow(
                    elevation = 1.dp,
                    shape = shape
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    shape = shape
                )
                .clip(shape)
                .heightIn(max = 108.dp)
                .aspectRatio(1f, true)
        )
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp)
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Text(
                text = track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Text(
                text = track.album ?: " ",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            if (showRecognitionDate) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = track.recognitionDate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .alpha(0.9f)
                        .align(Alignment.End)
                )
            }
        }
    }
}