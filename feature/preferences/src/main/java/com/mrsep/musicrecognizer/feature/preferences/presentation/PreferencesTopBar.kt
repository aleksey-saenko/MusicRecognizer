package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.mrsep.musicrecognizer.core.ui.components.ScreenScrollableTopBar
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreferencesTopBar(
    modifier: Modifier = Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    ScreenScrollableTopBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(StringsR.string.preferences).toUpperCase(Locale.current),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        scrollBehavior = topAppBarScrollBehavior
    )
}