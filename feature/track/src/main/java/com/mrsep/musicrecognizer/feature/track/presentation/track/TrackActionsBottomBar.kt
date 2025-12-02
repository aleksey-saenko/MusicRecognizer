package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.VinylRotating
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackActionsBottomBar(
    modifier: Modifier = Modifier,
    scrollBehavior: BottomAppBarScrollBehavior? = null,
    isFavorite: Boolean,
    isRetryAllowed: Boolean,
    isLyricsAvailable: Boolean,
    isLyricsLoading: Boolean,
    onFavoriteClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRetryRequested: () -> Unit,
) {
    val lyricsButtonAlpha by animateFloatAsState(
        when {
            isLyricsAvailable -> 1f
            isLyricsLoading -> 0.7f
            else -> 0.5f
        }
    )
    BottomAppBar(
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    modifier = Modifier.graphicsLayer { alpha = lyricsButtonAlpha },
                    onClick = onLyricsClick,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 3.dp,
                        focusedElevation = 3.dp,
                        hoveredElevation = 3.dp
                    ),
                ) {
                    Icon(
                        painter = painterResource(UiR.drawable.outline_lyrics_24),
                        contentDescription = stringResource(StringsR.string.show_lyrics)
                    )
                }
                AnimatedVisibility(
                    visible = isLyricsLoading,
                    enter = fadeIn(tween(300)) + scaleIn(tween(300)),
                    exit = fadeOut(tween(300)) + scaleOut(tween(300)),
                    modifier = Modifier
                        .padding(2.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    VinylRotating(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            if (isRetryAllowed) {
                IconButton(onClick = onRetryRequested) {
                    Icon(
                        painter = painterResource(UiR.drawable.outline_replay_24),
                        contentDescription = stringResource(StringsR.string.button_retry_recognition)
                    )
                }
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    painter = painterResource(UiR.drawable.outline_travel_explore_24),
                    contentDescription = stringResource(StringsR.string.web_search)
                )
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    painter = painterResource(
                        if (isFavorite) {
                            UiR.drawable.outline_favorite_fill1_24
                        } else {
                            UiR.drawable.outline_favorite_24
                        }
                    ),
                    contentDescription = if (isFavorite) {
                        stringResource(StringsR.string.unmark_track_as_favorite)
                    } else {
                        stringResource(StringsR.string.mark_track_as_favorite)
                    }
                )
            }
        }
    )
}
