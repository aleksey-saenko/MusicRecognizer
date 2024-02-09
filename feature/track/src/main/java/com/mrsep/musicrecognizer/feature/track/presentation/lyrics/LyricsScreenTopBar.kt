package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(StringsR.string.back)
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
                                    painter = painterResource(UiR.drawable.baseline_pause_24),
                                    contentDescription = stringResource(StringsR.string.stop_autoscroll)
                                )
                            }
                        } else {
                            IconButton(onClick = onLaunchAutoScrollClick) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = stringResource(StringsR.string.start_autoscroll)
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(StringsR.string.share)
                    )
                }
                IconButton(onClick = onChangeTextStyleClick) {
                    Icon(
                        painter = painterResource(UiR.drawable.baseline_text_fields_24),
                        contentDescription = stringResource(StringsR.string.lyrics_text_style)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}