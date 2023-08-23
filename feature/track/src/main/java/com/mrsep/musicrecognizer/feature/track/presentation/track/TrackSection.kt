package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    if (isExpandedScreen) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            AlbumArtwork(
                artworkUrl = artworkUrl,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)
                    .sizeIn(maxWidth = 600.dp)
            )
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TrackInfoColumn(
                    title = title,
                    artist = artist,
                    albumYear = albumAndYear,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                )
                ServicesChipsLazyRow(
                    links = links,
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
                artworkUrl = artworkUrl,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .sizeIn(maxWidth = 600.dp)
            )
            TrackInfoColumn(
                title = title,
                artist = artist,
                albumYear = albumAndYear,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 32.dp)
            )
            ServicesChipsLazyRow(
                links = links,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

        }
    }

}