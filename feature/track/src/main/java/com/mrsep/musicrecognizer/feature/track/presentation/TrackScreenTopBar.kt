package com.mrsep.musicrecognizer.feature.track.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.components.ScreenScrollableTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackScreenTopBar(
    modifier: Modifier = Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    ScreenScrollableTopBar(
        modifier = modifier,
        title = {
            Text(
                text = "TRACK".toUpperCase(Locale.current),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        topAppBarScrollBehavior = topAppBarScrollBehavior
    )
}