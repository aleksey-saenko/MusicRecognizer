package com.mrsep.musicrecognizer.presentation.screens.preferences.about

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

private const val ABOUT_ROUTE = "about"

fun NavGraphBuilder.aboutScreen() {
    composable(ABOUT_ROUTE) {
        AboutScreen()
    }
}

fun NavController.navigateToAboutScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = ABOUT_ROUTE, navOptions = navOptions)
}