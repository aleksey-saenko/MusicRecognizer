package com.mrsep.musicrecognizer.presentation.screens.preferences.queue

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val QUEUE_ROUTE = "queue"

fun NavGraphBuilder.queueScreen(
    onBackPressed: () -> Unit,
    onNavigateToTrackScreen: (trackMbId: String) -> Unit
) {
    composable(QUEUE_ROUTE) {
        QueueScreen(
            onBackPressed = onBackPressed,
            onNavigateToTrackScreen = onNavigateToTrackScreen
        )
    }
}

fun NavController.navigateToQueueScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = QUEUE_ROUTE, navOptions = navOptions)
}