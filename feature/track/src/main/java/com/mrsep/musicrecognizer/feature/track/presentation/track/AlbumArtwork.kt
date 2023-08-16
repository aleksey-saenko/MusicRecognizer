package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.mrsep.musicrecognizer.core.ui.R
import com.mrsep.musicrecognizer.core.ui.components.VinylRotating
import com.mrsep.musicrecognizer.core.ui.components.VinylStatic
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter

@Composable
internal fun AlbumArtwork(
    artworkUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val placeholder = forwardingPainter(
        painter = painterResource(R.drawable.baseline_album_24),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        alpha = 0.3f
    )
    AsyncImage(
        model = artworkUrl,
        error = placeholder,
        fallback = placeholder,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                shape = MaterialTheme.shapes.extraLarge
            )
    )
}

@Composable
internal fun AlbumArtworkSubcompose(
    artworkUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    // no fallback image? it looks not cool
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(artworkUrl)
            .fallback(R.drawable.baseline_album_24)
            .crossfade(true)
            .build(),
        loading = {
            Box(contentAlignment = Alignment.Center) {
                val visibleState = remember {
                    MutableTransitionState(false).apply { targetState = true }
                }
                val appearanceDelay = 1500
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = fadeIn(tween(delayMillis = appearanceDelay)) +
                            scaleIn(tween(delayMillis = appearanceDelay)),
                    exit = fadeOut(tween(delayMillis = appearanceDelay)) +
                            scaleOut(tween(delayMillis = appearanceDelay)),
                ) {
                    VinylRotating(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize(0.75f)
                            .alpha(0.3f)
                    )
                }
            }
        },
        error = {
            Box(contentAlignment = Alignment.Center) {
                VinylStatic(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                        .alpha(0.3f)
                )
            }
        },
        onError = { },
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = MaterialTheme.shapes.extraLarge
            )
    )
}