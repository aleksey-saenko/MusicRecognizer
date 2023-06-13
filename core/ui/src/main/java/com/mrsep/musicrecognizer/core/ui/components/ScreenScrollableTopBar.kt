package com.mrsep.musicrecognizer.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ScreenScrollableTopBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    val isTotalExpanded by remember {
        derivedStateOf { topAppBarScrollBehavior.state.collapsedFraction < 0.7f }
    }
    val transition = updateTransition(targetState = isTotalExpanded, label = "isTotalExpanded")

    TopAppBar(
        title = {
            transition.AnimatedVisibility(
                visible = { it },
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut() + scaleOut(targetScale = 0.95f),
//                enter = slideInVertically { totalHeight -> -totalHeight } + fadeIn(),
//                exit = slideOutVertically { totalHeight -> -totalHeight } + fadeOut(),
                content = { title() }
            )
        },
        navigationIcon = {
            transition.AnimatedVisibility(
                visible = { it },
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut() + scaleOut(targetScale = 0.95f),
//                enter = slideInHorizontally { totalWidth -> -totalWidth } + fadeIn(),
//                exit = slideOutHorizontally { totalWidth -> -totalWidth } + fadeOut()
                content = { navigationIcon() }
            )
        },
        actions = {
            transition.AnimatedVisibility(
                visible = { it },
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut() + scaleOut(targetScale = 0.95f),
//                enter = slideInHorizontally { totalWidth -> totalWidth } + fadeIn(),
//                exit = slideOutHorizontally { totalWidth -> totalWidth } + fadeOut()
                content = { actions() }
            )
        },
        scrollBehavior = topAppBarScrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Unspecified,
            scrolledContainerColor = Color.Unspecified,
        ),
        modifier = modifier
    )

}