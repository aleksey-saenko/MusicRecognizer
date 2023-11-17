package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    onTrackClick: (mbId: String) -> Unit,
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
        items(count = trackList.size, key = { trackList[it].mbId }) { index ->
            LazyGridTrackItem(
                track = trackList[index],
                selected = multiSelectionState.isSelected(trackList[index].mbId),
                multiselectEnabled = multiSelectionState.multiselectEnabled,
                shape = MaterialTheme.shapes.large,
                onTrackClick = { trackMbId ->
                    if (multiSelectionState.multiselectEnabled) {
                        multiSelectionState.toggleSelection(trackMbId)
                    } else {
                        onTrackClick(trackMbId)
                    }
                },
                onLongClick = multiSelectionState::toggleSelection,
                modifier = Modifier.animateItemPlacement(
                    tween(300)
                )

            )
        }
    }
    if (trackList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(StringsR.string.no_tracks_match_filter),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyGridTrackItem(
    track: TrackUi,
    selected: Boolean,
    multiselectEnabled: Boolean,
    shape: Shape,
    onTrackClick: (mbId: String) -> Unit,
    onLongClick: (mbId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.background
//            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        },
        label = "containerColor"
    )
    Column(
        modifier = modifier
            .background(color = containerColor, shape = shape)
            .clip(shape)
            .combinedClickable(
                onLongClick = { onLongClick(track.mbId) },
                onClick = { onTrackClick(track.mbId) },
                indication = if (multiselectEnabled) null else LocalIndication.current,
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
            placeholder = placeholder,
            error = placeholder,
            contentDescription = stringResource(StringsR.string.artwork),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .shadow(elevation = 1.dp, shape = shape)
                .background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    shape = shape
                )
                .clip(shape)
                .aspectRatio(1f)

        )
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
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