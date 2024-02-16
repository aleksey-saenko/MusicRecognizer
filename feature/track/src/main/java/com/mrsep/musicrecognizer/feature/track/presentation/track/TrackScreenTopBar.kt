package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

internal enum class SearchProvider { WebDefault, Wikipedia }
internal enum class SearchTarget { Track, Artist, Album }

internal data class SearchParams(
    val provider: SearchProvider,
    val target: SearchTarget
)

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
    onPerformWebSearchClick: (SearchParams) -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        modifier = modifier,
        title = {},
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    painter = painterResource(UiR.drawable.outline_arrow_back_24),
                    contentDescription = stringResource(StringsR.string.back)
                )
            }
        },
        actions = {
            Row {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        painter = painterResource(
                            if (isFavorite)
                                UiR.drawable.outline_favorite_fill1_24
                            else
                                UiR.drawable.outline_favorite_24
                        ),
                        contentDescription = if (isFavorite)
                            stringResource(StringsR.string.mark_as_favorite)
                        else
                            stringResource(StringsR.string.unmark_as_favorite)
                    )
                }
                if (isLyricsAvailable) {
                    IconButton(onClick = onLyricsClick) {
                        Icon(
                            painter = painterResource(UiR.drawable.outline_lyrics_24),
                            contentDescription = stringResource(StringsR.string.show_lyrics)
                        )
                    }
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        painter = painterResource(UiR.drawable.outline_share_24),
                        contentDescription = stringResource(StringsR.string.share)
                    )
                }
                var menuExpanded by remember { mutableStateOf(false) }
                var menuSearchMode by remember { mutableStateOf(false) }
                var searchProvider = remember { SearchProvider.WebDefault }
                Box {
                    IconButton(onClick = { menuSearchMode = false; menuExpanded = !menuExpanded }) {
                        Icon(
                            painter = painterResource(UiR.drawable.outline_more_vert_24),
                            contentDescription = stringResource(StringsR.string.show_more)
                        )
                    }
                    // workaround to change hardcoded shape of menu https://issuetracker.google.com/issues/283654243
                    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.small)) {
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            if (!menuSearchMode) {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(StringsR.string.web_search))
                                    },
                                    onClick = {
                                        searchProvider = SearchProvider.WebDefault
                                        menuSearchMode = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(UiR.drawable.outline_travel_explore_24),
                                            contentDescription = null
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(StringsR.string.wikipedia))
                                    },
                                    onClick = {
                                        searchProvider = SearchProvider.Wikipedia
                                        menuSearchMode = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(UiR.drawable.wikipedia_logo_fill0),
                                            contentDescription = null
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(StringsR.string.show_more)) },
                                    onClick = {
                                        menuExpanded = false
                                        onShowDetailsClick()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(UiR.drawable.outline_info_24),
                                            contentDescription = null
                                        )
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(StringsR.string.delete)) },
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteClick()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(UiR.drawable.outline_delete_24),
                                            contentDescription = null
                                        )
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(StringsR.string.search_for)) },
                                    onClick = {},
                                    enabled = false,
                                    colors = MenuDefaults.itemColors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                                            .copy(alpha = 0.75f)
                                    )
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(StringsR.string.track)) },
                                    onClick = {
                                        menuExpanded = false
                                        onPerformWebSearchClick(
                                            SearchParams(searchProvider, SearchTarget.Track)
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(StringsR.string.artist)) },
                                    onClick = {
                                        menuExpanded = false
                                        onPerformWebSearchClick(
                                            SearchParams(searchProvider, SearchTarget.Artist)
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(StringsR.string.album)) },
                                    onClick = {
                                        menuExpanded = false
                                        onPerformWebSearchClick(
                                            SearchParams(searchProvider, SearchTarget.Album)
                                        )
                                    },
                                )
                            }

                        }
                    }
                }

            }
        },
        scrollBehavior = scrollBehavior
    )
}