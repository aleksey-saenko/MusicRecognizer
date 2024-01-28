package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.mrsep.musicrecognizer.core.ui.components.MultiSelectionState
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import kotlinx.collections.immutable.ImmutableList
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackLazyGrid(
    trackList: ImmutableList<TrackUi>,
    onTrackClick: (trackId: String) -> Unit,
    lazyGridState: LazyGridState,
    multiSelectionState: MultiSelectionState<String>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 108.dp),
        state = lazyGridState,
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(items = trackList, key = { track -> track.id }) { track ->
            LazyGridTrackItem(
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
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyGridTrackItem(
    track: TrackUi,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large
) {
    Column(
        modifier = modifier
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.background
                },
                shape = shape
            )
            .clip(shape)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick,
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(4.dp)
            .fillMaxSize()
    ) {
        val placeholder = forwardingPainter(
            painter = painterResource(UiR.drawable.baseline_album_24),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            alpha = 0.2f
        )
        AsyncImage(
            model = track.artworkUrl,
            fallback = placeholder,
            error = placeholder,
            contentDescription = stringResource(StringsR.string.artwork),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    shape = shape
                )
                .clip(shape)
                .aspectRatio(1f)

        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.95f)
            )
        }
    }
}