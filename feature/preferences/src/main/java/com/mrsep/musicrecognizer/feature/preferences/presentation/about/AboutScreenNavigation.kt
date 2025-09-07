package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object AboutScreenNavigation {

    const val ROUTE = "about"

    fun NavGraphBuilder.aboutScreen(
        onBackPressed: () -> Unit,
        onNavigateToSoftwareScreen: (from: NavBackStackEntry) -> Unit,
        onNavigateToAppLicenseScreen: (from: NavBackStackEntry) -> Unit,
    ) {
        composable(ROUTE) { backStackEntry ->
            AboutScreen(
                onBackPressed = onBackPressed,
                onNavigateToSoftwareScreen = { onNavigateToSoftwareScreen(backStackEntry) },
                onNavigateToAppLicenseScreen = { onNavigateToAppLicenseScreen(backStackEntry) },
            )
        }
    }

    fun NavController.navigateToAboutScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }
}
