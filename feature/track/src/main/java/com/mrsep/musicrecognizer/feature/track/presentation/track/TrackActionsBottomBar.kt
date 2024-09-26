package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    onFavoriteClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRetryRequested: () -> Unit,
) {
    BottomAppBar(
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        floatingActionButton = {
            if (isLyricsAvailable) {
                FloatingActionButton(
                    onClick = onLyricsClick,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp
                    ),
                ) {
                    Icon(
                        painter = painterResource(UiR.drawable.outline_lyrics_24),
                        contentDescription = stringResource(StringsR.string.show_lyrics)
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
