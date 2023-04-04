package com.mrsep.musicrecognizer.presentation.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

@Composable
fun TrackLazyRow(
    trackList: ImmutableList<Track>,
    onTrackClick: (mbId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
    ) {
        items(items = trackList, key = { it.mbId }) { track ->
            LazyRowTrackItem(
                track = track,
                onTrackClick = onTrackClick,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun LazyRowTrackItem(
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
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = track.artist,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .alpha(0.8f)
                .padding(top = 4.dp, bottom = 8.dp)
        )
    }

}

@Composable
fun LazyRowTrackItemHorizontal(
    track: Track,
    modifier: Modifier = Modifier,
    onTrackClick: (mbId: String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = modifier.height(152.dp).padding(8.dp) //.width(336.dp)
            .background(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            shape = MaterialTheme.shapes.medium
        )
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
                .clip(MaterialTheme.shapes.medium)
                .aspectRatio(1f)
        )
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
            )
            Text(
                text = track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .alpha(0.8f)
                    .padding(top = 4.dp, bottom = 8.dp)
            )
        }
    }
}