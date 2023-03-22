package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val HOME_ROUTE = "home"

fun NavGraphBuilder.homeScreen(
    onNavigateToTrackScreen: (mbId: String) -> Unit
) {
    composable(HOME_ROUTE) {
        HomeScreen(
            onNavigateToTrackScreen = onNavigateToTrackScreen
        )
    }
}

fun NavController.navigateToHomeScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = HOME_ROUTE, navOptions = navOptions)
}