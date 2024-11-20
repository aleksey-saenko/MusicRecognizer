package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object SoftwareScreenNavigation {

    private const val ROUTE = "third_party_software"

    fun NavGraphBuilder.softwareScreen(
        onBackPressed: () -> Unit,
        onNavigateToSoftwareDetailsScreen: (uniqueId: String, from: NavBackStackEntry) -> Unit,
    ) {
        composable(ROUTE) { backStackEntry ->
            SoftwareScreen(
                onBackPressed = onBackPressed,
                onNavigateToSoftwareDetailsScreen = { uniqueId ->
                    onNavigateToSoftwareDetailsScreen(uniqueId, backStackEntry)
                },
            )
        }
    }

    fun NavController.navigateToSoftwareScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }
}
