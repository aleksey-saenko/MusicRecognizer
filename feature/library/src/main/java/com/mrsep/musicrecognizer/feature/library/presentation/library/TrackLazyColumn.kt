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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.core.ui.R
import com.mrsep.musicrecognizer.core.ui.components.MultiSelectionState
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackLazyColumn(
    trackList: ImmutableList<TrackUi>,
    onTrackClick: (mbId: String) -> Unit,
    lazyListState: LazyListState,
    multiSelectionState: MultiSelectionState<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(top = 6.dp, start = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items = trackList, key = { track -> track.mbId }) { track ->
            LazyListTrackItem(
                track = track,
                selected = multiSelectionState.isSelected(track.mbId),
                multiselectEnabled = multiSelectionState.multiselectEnabled,
                onClick = {
                    if (multiSelectionState.multiselectEnabled) {
                        multiSelectionState.toggleSelection(track.mbId)
                    } else {
                        onTrackClick(track.mbId)
                    }
                },
                onLongClick = { multiSelectionState.toggleSelection(track.mbId) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyListTrackItem(
    track: TrackUi,
    selected: Boolean,
    multiselectEnabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        },
        label = "containerColor"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = containerColor, shape = shape)
            .clip(shape)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick,
                indication = if (multiselectEnabled) null else LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .fillMaxWidth()
    ) {
        val placeholder = forwardingPainter(
            painter = painterResource(R.drawable.baseline_album_24),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            alpha = 0.2f
        )
        AsyncImage(
            model = track.artworkUrl,
            placeholder = placeholder,
            error = placeholder,
            contentDescription = stringResource(com.mrsep.musicrecognizer.core.strings.R.string.artwork),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    shape = shape
                )
                .clip(shape)
                .heightIn(max = 112.dp)
                .aspectRatio(1f, true)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.9f)
            )
            Text(
                text = track.albumAndYear ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.9f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = track.recognitionDate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .alpha(0.72f)
                    .align(Alignment.End)
            )
        }
    }
}