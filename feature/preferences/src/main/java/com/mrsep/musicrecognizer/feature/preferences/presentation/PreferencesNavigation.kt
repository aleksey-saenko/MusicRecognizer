package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object PreferencesScreen {

    const val ROUTE = "preferences"

    fun NavGraphBuilder.preferencesScreen(
        onNavigateToAboutScreen: (from: NavBackStackEntry) -> Unit,
        onNavigateToQueueScreen: (from: NavBackStackEntry) -> Unit,
        onNavigateToDeveloperScreen: (from: NavBackStackEntry) -> Unit
    ) {
        composable(ROUTE) { backStackEntry ->
            PreferencesScreen(
                onNavigateToAboutScreen = { onNavigateToAboutScreen(backStackEntry) },
                onNavigateToQueueScreen = { onNavigateToQueueScreen(backStackEntry) },
                onNavigateToDeveloperScreen = { onNavigateToDeveloperScreen(backStackEntry) }
            )
        }
    }

    fun NavController.navigateToPreferencesScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }

}