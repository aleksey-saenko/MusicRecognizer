package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object PreferencesScreen {

    const val ROUTE = "preferences"

    fun NavGraphBuilder.preferencesScreen(
        onNavigateToAboutScreen: () -> Unit,
        onNavigateToQueueScreen: () -> Unit
    ) {
        composable(ROUTE) {
            PreferencesScreen(
                onNavigateToAboutScreen = onNavigateToAboutScreen,
                onNavigateToQueueScreen = onNavigateToQueueScreen
            )
        }
    }

    fun NavController.navigateToPreferencesScreen(
        navOptions: NavOptions? = null
    ) {
        this.navigate(route = ROUTE, navOptions = navOptions)
    }

}

