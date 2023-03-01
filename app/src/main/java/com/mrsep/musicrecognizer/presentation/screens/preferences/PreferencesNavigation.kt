package com.mrsep.musicrecognizer.presentation.screens.preferences

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val PREFERENCES_ROUTE = "preferences"

fun NavGraphBuilder.preferencesScreen() {
    composable(PREFERENCES_ROUTE) {
        PreferencesScreen()
    }
}

fun NavController.navigateToPreferencesScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = PREFERENCES_ROUTE, navOptions = navOptions)
}