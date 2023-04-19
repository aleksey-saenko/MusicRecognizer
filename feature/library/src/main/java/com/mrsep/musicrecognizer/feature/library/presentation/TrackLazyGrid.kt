package com.mrsep.musicrecognizer.feature.library.presentation

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackLazyGrid(
    trackList: ImmutableList<Track>,
    onTrackClick: (mbId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyGridState()
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
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        state = state,
        modifier = modifier
    ) {
        items(count = trackList.size, key = { trackList[it].mbId }) { index ->
            LazyGridTrackItem(
                track = trackList[index],
                onTrackClick = onTrackClick,
                modifier = Modifier.animateItemPlacement(
                    tween(300)
                )

            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackLazyGridHorizontal(
    trackList: ImmutableList<Track>,
    restCount: Int,
    onTrackClick: (mbId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyGridState()
    LazyHorizontalGrid(
//        rows = GridCells.Fixed(2),
        rows = GridCells.Adaptive(minSize = 160.dp),
        state = state,
        modifier = modifier, //.height(250.dp)
        userScrollEnabled = true
    ) {
        items(count = trackList.size, key = { trackList[it].mbId }) { index ->
            LazyRowTrackItem(
                track = trackList[index],
                onTrackClick = onTrackClick,
                modifier = Modifier.animateItemPlacement(tween(300))
            )
            LazyRowTrackItemHorizontal(
                track = trackList[index],
                onTrackClick = onTrackClick,
                modifier = Modifier.animateItemPlacement(tween(300))
            )
        }
        if (restCount > 0) {
            restCountLabel(
                restCount = restCount,
                onLabelClick = {},
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

private fun LazyGridScope.restCountLabel(
    modifier: Modifier = Modifier,
    restCount: Int,
    onLabelClick: () -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }) {
        RestCountLabel(
            modifier = modifier,
            restCount = restCount,
            onLabelClick = onLabelClick
        )
    }
}

@Composable
internal fun RestCountLabel(
    modifier: Modifier = Modifier,
    restCount: Int,
    onLabelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "+$restCount",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                .clickable(onClick = onLabelClick)
                .padding(vertical = 8.dp, horizontal = 12.dp)
        )
    }
}


@Composable
internal fun LazyGridTrackItem(
    track: Track,
    modifier: Modifier = Modifier,
    onTrackClick: (mbId: String) -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onTrackClick(track.mbId) }
            .padding(8.dp)
            .width(104.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        val placeholder = forwardingPainter(
            painter = painterResource(UiR.drawable.baseline_album_24),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            alpha = 0.3f
        )
        AsyncImage(
            model = track.links.artwork,
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

@Composable
private fun LazyGridState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}