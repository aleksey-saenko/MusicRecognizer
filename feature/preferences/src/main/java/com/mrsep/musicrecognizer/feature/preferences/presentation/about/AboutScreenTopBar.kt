package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
internal fun AboutScreenTopBar(
    modifier: Modifier = Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onBackPressed: () -> Unit
) {
    ScreenScrollableTopBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(StringsR.string.about).toUpperCase(Locale.current),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = topAppBarScrollBehavior
    )
}