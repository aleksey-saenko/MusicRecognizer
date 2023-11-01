package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun TrackSection(
    title: String,
    artist: String,
    albumAndYear: String?,
    artworkUrl: String?,
    links: ImmutableList<ServiceLink>,
    isExpandedScreen: Boolean,
    onArtworkCached: (Uri) -> Unit,
    createSeedColor: Boolean,
    onSeedColor: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isExpandedScreen) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            AlbumArtwork(
                url = artworkUrl,
                onArtworkCached = onArtworkCached,
                createSeedColor = createSeedColor,
                onSeedColorCreated = onSeedColor,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)
                    .sizeIn(maxWidth = 600.dp)
                    .fillMaxWidth(0.33f)
                    .aspectRatio(1f)
            )
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TrackInfoColumn(
                    title = title,
                    artist = artist,
                    albumYear = albumAndYear,
                    isExpandedScreen = true,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                ServicesChipsLazyRow(
                    links = links,
                    showOnlyIcons = false,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlbumArtwork(
                url = artworkUrl,
                onArtworkCached = onArtworkCached,
                createSeedColor = createSeedColor,
                onSeedColorCreated = onSeedColor,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .sizeIn(maxWidth = 600.dp)
                    .weight(1f, false)
                    .aspectRatio(1f, false)
            )
            TrackInfoColumn(
                title = title,
                artist = artist,
                albumYear = albumAndYear,
                isExpandedScreen = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
            ServicesChipsLazyRow(
                links = links,
                showOnlyIcons = false,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp)
            )
        }
    }

}