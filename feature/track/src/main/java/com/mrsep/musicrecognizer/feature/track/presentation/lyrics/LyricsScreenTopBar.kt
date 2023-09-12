package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.ui.components.ScreenScrollableTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreenTopBar(
    onBackPressed: () -> Unit,
    onShareClick: () -> Unit,
    onChangeTextStyleClick: () -> Unit,
    modifier: Modifier = Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    ScreenScrollableTopBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(StringsR.string.back)
                )
            }
        },
        actions = {
            Row {
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
        scrollBehavior = topAppBarScrollBehavior
    )
}