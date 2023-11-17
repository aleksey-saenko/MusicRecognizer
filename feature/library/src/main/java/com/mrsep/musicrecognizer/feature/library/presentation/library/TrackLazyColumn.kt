package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.draw.shadow
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
        contentPadding = PaddingValues(top = 6.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(count = trackList.size, key = { trackList[it].mbId }) { index ->
            LazyListTrackItem(
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyListTrackItem(
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
//            MaterialTheme.colorScheme.background
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        },
        label = "containerColor"
    )
    Row(
        modifier = modifier
            .background(color = containerColor, shape = shape)
            .clip(shape)
            .combinedClickable(
                onLongClick = { onLongClick(track.mbId) },
                onClick = { onTrackClick(track.mbId) },
                indication = if (multiselectEnabled) null else LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .height(120.dp)
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
                .padding(4.dp)
                .shadow(elevation = 1.dp, shape = shape)
                .background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    shape = shape
                )
                .clip(shape)
                .aspectRatio(1f)

        )
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
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
                modifier = Modifier.alpha(0.95f)
            )
            track.albumAndYear?.let { albumAndYear ->
                Text(
                    text = albumAndYear,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.95f)
                )
            }
        }
    }
}