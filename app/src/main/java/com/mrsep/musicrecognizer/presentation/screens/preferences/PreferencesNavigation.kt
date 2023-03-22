package com.mrsep.musicrecognizer.presentation.screens.preferences

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val PREFERENCES_ROUTE = "preferences"

fun NavGraphBuilder.preferencesScreen(navController: NavController) {
    composable(PREFERENCES_ROUTE) {
        PreferencesScreen(navController = navController)
    }
}

fun NavController.navigateToPreferencesScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = PREFERENCES_ROUTE, navOptions = navOptions)
}