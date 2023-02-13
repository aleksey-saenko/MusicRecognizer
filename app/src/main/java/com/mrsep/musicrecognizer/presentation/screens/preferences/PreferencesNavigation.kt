package com.mrsep.musicrecognizer.presentation.screens.preferences

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

private const val ROOT_ROUTE = "preferences"

fun NavGraphBuilder.preferencesScreen() {
    composable(ROOT_ROUTE) {
        PreferencesScreen()
    }
}

fun NavController.navigateToPreferencesScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = ROOT_ROUTE, navOptions = navOptions)
}