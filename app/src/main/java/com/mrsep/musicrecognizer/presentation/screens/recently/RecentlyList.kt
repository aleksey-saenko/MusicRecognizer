package com.mrsep.musicrecognizer.presentation.screens.recently

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.presentation.PreviewDeviceLight
import com.mrsep.musicrecognizer.presentation.fakeTrackList
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.util.forwardingPainter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun RecentlyList(
    recentTrackList: ImmutableList<Track>,
    onTrackClick: (mbId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
    ) {
        items(items = recentTrackList, key = { it.mbId }) {track ->
            RecentTrackItem(
                track = track,
                onTrackClick = onTrackClick,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun RecentTrackItem(
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
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

}

@Preview
@Composable
private fun ItemPreview() {
    MusicRecognizerTheme {
        Surface {
            RecentTrackItem(
                track = fakeTrackList[0],
                onTrackClick = { }
            )
        }
    }
}

@PreviewDeviceLight
@Composable
private fun RecentlyListPreview() {
    MusicRecognizerTheme {
        Surface {
            RecentlyList(
                modifier = Modifier.padding(16.dp),
                recentTrackList = fakeTrackList.toImmutableList(),
                onTrackClick = { }
            )
        }
    }
}