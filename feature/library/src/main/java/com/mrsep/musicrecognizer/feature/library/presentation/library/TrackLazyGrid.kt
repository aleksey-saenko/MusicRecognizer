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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackLazyGrid(
    trackList: ImmutableList<TrackUi>,
    onTrackClick: (mbId: String) -> Unit,
    lazyGridState: LazyGridState,
    selectionState: TrackSelectionState,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        state = lazyGridState,
        modifier = modifier
    ) {
        items(count = trackList.size, key = { trackList[it].mbId }) { index ->
            LazyGridTrackItem(
                track = trackList[index],
                selected = selectionState.isTrackSelected(trackList[index].mbId),
                multiselectEnabled = selectionState.multiselectEnabled,
                onTrackClick = { trackMbId ->
                    if (selectionState.multiselectEnabled) {
                        selectionState.toggleSelection(trackMbId)
                    } else {
                        onTrackClick(trackMbId)
                    }
                },
                onLongClick = selectionState::toggleSelection,
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
    onTrackClick: (mbId: String) -> Unit,
    onLongClick: (mbId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            Color.Unspecified,
        label = "containerColor"
    )
    Column(
        modifier = modifier
            .padding(2.dp)
            .background(
                color = containerColor,
                shape = MaterialTheme.shapes.large
            )
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onLongClick = { onLongClick(track.mbId) },
                onClick = { onTrackClick(track.mbId) },
                indication = if (multiselectEnabled) null else LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(6.dp)
            .width(104.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
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
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .aspectRatio(1f)
        )
        Text(
            text = track.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = track.artist,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )
    }
}