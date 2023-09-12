package com.mrsep.musicrecognizer.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScrollableTopBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior
) {
    // since the toolbar has collapsing behavior, we have to disable the icons to avoid false positives
    val isExpanded by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction < 0.6f }
    }
    val topBarAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        label = ""
    )
    TopAppBar(
        title = {
            Crossfade(targetState = isExpanded, label = "title") { expanded ->
                if (expanded) {
                    title()
                }
            }
        },
        navigationIcon = {
            Crossfade(targetState = isExpanded, label = "navigationIcon") { expanded ->
                if (expanded) {
                    navigationIcon()
                }
            }
        },
        actions = {
            Crossfade(targetState = isExpanded, label = "actions") { expanded ->
                if (expanded) {
                    actions()
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Unspecified,
            scrolledContainerColor = Color.Unspecified,
        ),
        modifier = modifier.alpha(topBarAlpha)
    )
}