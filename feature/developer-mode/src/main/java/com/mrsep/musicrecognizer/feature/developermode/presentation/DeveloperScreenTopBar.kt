package com.mrsep.musicrecognizer.feature.developermode.presentation

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.mrsep.musicrecognizer.core.ui.components.ScreenScrollableTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeveloperScreenTopBar(
    modifier: Modifier = Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onBackPressed: () -> Unit
) {
    ScreenScrollableTopBar(
        modifier = modifier,
        title = {
            Text(
                text = "Developer options".toUpperCase(Locale.current),
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