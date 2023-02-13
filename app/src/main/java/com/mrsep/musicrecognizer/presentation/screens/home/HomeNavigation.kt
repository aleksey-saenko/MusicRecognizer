package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

private const val ROOT_ROUTE = "home"

fun NavGraphBuilder.homeScreen() {
    composable(ROOT_ROUTE) {
        HomeScreen()
    }
}

fun NavController.navigateToHomeScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = ROOT_ROUTE, navOptions = navOptions)
}