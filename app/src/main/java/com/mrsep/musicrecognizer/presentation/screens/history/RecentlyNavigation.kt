package com.mrsep.musicrecognizer.presentation.screens.history

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

private const val ROOT_ROUTE = "recently"

fun NavGraphBuilder.recentlyScreen(
    onTrackClick: (mbId: String) -> Unit
) {
    composable(ROOT_ROUTE) {
        RecentlyScreen(onTrackClick = onTrackClick)
    }
}

fun NavController.navigateToRecentlyScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = ROOT_ROUTE, navOptions = navOptions)
}