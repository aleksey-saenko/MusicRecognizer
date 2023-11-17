package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableDefaults.VelocityThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

private enum class DismissState { NoAction, Dismissed }

@OptIn(ExperimentalMaterialApi::class)
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
    isRetryAvailable: Boolean,
    onRetryRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val swipeableState = rememberSwipeableState(
        initialValue = DismissState.NoAction,
        animationSpec = tween(),
        confirmStateChange = { state ->
            if (state == DismissState.Dismissed) onRetryRequested()
            true
        }
    )
    val sizePx = with(density) { -100.dp.toPx() }
    val anchors = mapOf(
        0f to DismissState.NoAction,
        sizePx to DismissState.Dismissed
    )
    val scaleFactor by animateFloatAsState(
        targetValue = if (swipeableState.targetValue == DismissState.Dismissed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "SwipeScaleFactor"
    )
    Box(
        modifier = modifier
            .swipeable(
                enabled = isRetryAvailable,
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.9f) },
                orientation = Orientation.Vertical,
                velocityThreshold = VelocityThreshold * 5
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        if (isExpandedScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scaleFactor),
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
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scaleFactor),
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
                        .padding(top = 24.dp, bottom = 24.dp)
                )
            }
        }
    }

}