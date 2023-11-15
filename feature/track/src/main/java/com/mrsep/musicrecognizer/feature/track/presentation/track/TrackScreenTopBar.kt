package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.ui.components.ScreenScrollableTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackScreenTopBar(
    onBackPressed: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    isLyricsAvailable: Boolean,
    onLyricsClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShowDetailsClick: () -> Unit,
    onOpenOdesliClick: () -> Unit,
    modifier: Modifier = Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    ScreenScrollableTopBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(StringsR.string.back)
                )
            }
        },
        actions = {
            Row {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite)
                            stringResource(StringsR.string.mark_as_favorite)
                        else
                            stringResource(StringsR.string.unmark_as_favorite)
                    )
                }
                if (isLyricsAvailable) {
                    IconButton(onClick = onLyricsClick) {
                        Icon(
                            painter = painterResource(UiR.drawable.baseline_lyrics_24),
                            contentDescription = stringResource(StringsR.string.show_lyrics)
                        )
                    }
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(StringsR.string.share)
                    )
                }
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = !menuExpanded }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(StringsR.string.show_more)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(StringsR.string.open_odesli)) },
                            onClick = {
                                menuExpanded = false
                                onOpenOdesliClick()
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(UiR.drawable.baseline_travel_explore_24),
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(StringsR.string.show_more)) },
                            onClick = {
                                menuExpanded = false
                                onShowDetailsClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null
                                )
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text(text = stringResource(StringsR.string.delete)) },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

            }
        },
        scrollBehavior = topAppBarScrollBehavior
    )
}