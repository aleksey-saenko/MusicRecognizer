package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.track.domain.model.TrackLink
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun TrackSection(
    title: String,
    artist: String,
    album: String?,
    year: String?,
    artworkUrl: String?,
    isLoadingLinks: Boolean,
    trackLinks: ImmutableList<TrackLink>,
    isExpandedScreen: Boolean,
    onArtworkCached: (Uri) -> Unit,
    createSeedColor: Boolean,
    onSeedColor: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isExpandedScreen) {
        Row(modifier = modifier) {
            AlbumArtwork(
                url = artworkUrl,
                onArtworkCached = onArtworkCached,
                createSeedColor = createSeedColor,
                onSeedColorCreated = onSeedColor,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(0.33f)
                    .aspectRatio(1f)
            )
            Column(modifier = Modifier.padding(end = 16.dp)) {
                TrackInfoColumn(
                    title = title,
                    artist = artist,
                    album = album,
                    year = year,
                    modifier = Modifier
                        .animateContentSize()
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                MusicServiceChipsFlowRow(
                    isLoading = isLoadingLinks,
                    trackLinks = trackLinks,
                    modifier = Modifier
                        .animateContentSize(animationSpec = tween(2000))
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    } else {
        Column(
            modifier = modifier,
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
                    .aspectRatio(1f),
            )
            Spacer(Modifier.height(16.dp))
            TrackInfoColumn(
                title = title,
                artist = artist,
                album = album,
                year = year,
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            MusicServiceChipsFlowRow(
                isLoading = isLoadingLinks,
                trackLinks = trackLinks,
                modifier = Modifier
                    .animateContentSize(animationSpec = tween(500))
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@PreviewScreenSizes
@Composable
private fun Preview() {
    MusicRecognizerTheme(dynamicColor = false) {
        TrackSection(
            title = "Track title",
            artist = "Track artist",
            album = "Track album",
            year = "2024",
            artworkUrl = null,
            trackLinks = MusicService.entries.map { TrackLink("", it) }.toImmutableList(),
            isLoadingLinks = false,
            isExpandedScreen = true,
            onArtworkCached = {},
            createSeedColor = false,
            onSeedColor = {},
            modifier = Modifier.verticalScroll(rememberScrollState())
        )
    }
}