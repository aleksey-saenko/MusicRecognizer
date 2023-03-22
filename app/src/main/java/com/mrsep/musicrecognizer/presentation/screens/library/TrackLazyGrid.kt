package com.mrsep.musicrecognizer.presentation.screens.library

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.util.forwardingPainter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackLazyGrid(
    trackList: ImmutableList<Track>,
    onTrackClick: (mbId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyGridState()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        state = state,
        modifier = modifier,
    ) {
        items(count = trackList.size, key = { trackList[it].mbId }) { index ->
            LazyGridTrackItem(
                track = trackList[index],
                onTrackClick = onTrackClick,
                modifier = Modifier.animateItemPlacement(
                    tween(3000)
                )

            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackLazyGridHorizontal(
    trackList: ImmutableList<Track>,
    onTrackClick: (mbId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyGridState()
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        state = state,
        modifier = modifier.height(250.dp),
        userScrollEnabled = false
    ) {
        items(count = 4, key = { trackList[it].mbId }) { index ->
            LazyRowTrackItemHorizontal(
                track = trackList[index],
                onTrackClick = onTrackClick,
                modifier = Modifier.animateItemPlacement(
                    tween(3000)
                )

            )
        }
    }
}

@Composable
fun LazyGridTrackItem(
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
            painter = painterResource(R.drawable.baseline_album_96),
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
            modifier = Modifier.padding(top = 8.dp)
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