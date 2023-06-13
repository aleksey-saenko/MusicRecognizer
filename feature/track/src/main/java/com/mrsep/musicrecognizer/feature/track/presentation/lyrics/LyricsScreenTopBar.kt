package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
            IconButton(onClick = { }) {
                Icon(
                    painter = painterResource(id = UiR.drawable.baseline_text_fields_24),
                    contentDescription = "Not implemented"
                )
            }
        },
        topAppBarScrollBehavior = topAppBarScrollBehavior
    )
}