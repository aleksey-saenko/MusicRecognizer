package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreenTopBar(
    autoScrollAvailable: Boolean,
    autoScrollStarted: Boolean,
    onBackPressed: () -> Unit,
    onShareClick: () -> Unit,
    onChangeTextStyleClick: () -> Unit,
    onLaunchAutoScrollClick: () -> Unit,
    onStopAutoScrollClick: () -> Unit,
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
                    contentDescription = stringResource(StringsR.string.nav_back)
                )
            }
        },
        actions = {
            Row {
                if (autoScrollAvailable) {
                    AnimatedContent(
                        targetState = autoScrollStarted,
                        label = "AutoScrollButton"
                    ) { started ->
                        if (started) {
                            IconButton(onClick = onStopAutoScrollClick) {
                                Icon(
                                    painter = painterResource(UiR.drawable.outline_pause_fill1_24),
                                    contentDescription = stringResource(StringsR.string.lyrics_stop_autoscroll)
                                )
                            }
                        } else {
                            IconButton(onClick = onLaunchAutoScrollClick) {
                                Icon(
                                    painter = painterResource(UiR.drawable.outline_play_arrow_fill1_24),
                                    contentDescription = stringResource(StringsR.string.lyrics_start_autoscroll)
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        painter = painterResource(UiR.drawable.outline_share_24),
                        contentDescription = stringResource(StringsR.string.share)
                    )
                }
                IconButton(onClick = onChangeTextStyleClick) {
                    Icon(
                        painter = painterResource(UiR.drawable.outline_format_size_24),
                        contentDescription = stringResource(StringsR.string.text_style)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreenLoadingTopBar(
    onBackPressed: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {},
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    painter = painterResource(UiR.drawable.outline_arrow_back_24),
                    contentDescription = stringResource(StringsR.string.nav_back)
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
