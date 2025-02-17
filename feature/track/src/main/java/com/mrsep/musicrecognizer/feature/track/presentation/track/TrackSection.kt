package com.mrsep.musicrecognizer.feature.track.presentation.track

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
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun TrackSection(
    track: TrackUi,
    isLoadingLinks: Boolean,
    isExpandedScreen: Boolean,
    onArtworkClick: () -> Unit,
    createSeedColor: Boolean,
    onSeedColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isExpandedScreen) {
        Row(modifier = modifier) {
            AlbumArtwork(
                url = track.artworkUrl,
                onArtworkClick = onArtworkClick,
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
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    year = track.year,
                    modifier = Modifier
                        .animateContentSize()
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                MusicServiceChipsFlowRow(
                    isLoading = isLoadingLinks,
                    trackLinks = track.trackLinks,
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
                url = track.artworkUrl,
                onArtworkClick = onArtworkClick,
                createSeedColor = createSeedColor,
                onSeedColorCreated = onSeedColor,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .sizeIn(maxWidth = 600.dp)
                    .aspectRatio(1f),
            )
            Spacer(Modifier.height(16.dp))
            TrackInfoColumn(
                title = track.title,
                artist = track.artist,
                album = track.album,
                year = track.year,
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            MusicServiceChipsFlowRow(
                isLoading = isLoadingLinks,
                trackLinks = track.trackLinks,
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
            track = TrackUi(
                id = "1",
                title = "Track title",
                artist = "Track artist",
                album = "Track album",
                year = "2024",
                artworkUrl = null,
                trackLinks = MusicService.entries.map { TrackLink("", it) }.toImmutableList(),
                isFavorite = false,
                duration = "2:45",
                recognizedAt = "2:22",
                recognizedBy = RecognitionProvider.Audd,
                lastRecognitionDate = "Now",
                themeSeedColor = null,
                lyrics = null
            ),
            isLoadingLinks = false,
            isExpandedScreen = true,
            onArtworkClick = {},
            createSeedColor = false,
            onSeedColor = {},
            modifier = Modifier.verticalScroll(rememberScrollState())
        )
    }
}
