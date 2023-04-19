package com.mrsep.musicrecognizer.feature.library.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
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
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import kotlinx.collections.immutable.ImmutableList
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun TrackLazyRow(
    trackList: ImmutableList<Track>,
    restCount: Int,
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
        if (restCount > 0) {
            restCountLabel(
                restCount = restCount,
                onLabelClick = {},
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

private fun LazyListScope.restCountLabel(
    modifier: Modifier = Modifier,
    restCount: Int,
    onLabelClick: () -> Unit
) {
    item {
        RestCountLabel(
            modifier = modifier,
            restCount = restCount,
            onLabelClick = onLabelClick
        )
    }
}

@Composable
internal fun LazyRowTrackItem(
    track: Track,
    modifier: Modifier = Modifier,
    onTrackClick: (mbId: String) -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onTrackClick(track.mbId) }
            .padding(8.dp)
            .width(IntrinsicSize.Min),
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
                .weight(1f, false)
        )
        Text(
            text = track.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(top = 12.dp)
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
internal fun LazyRowTrackItemHorizontal(
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