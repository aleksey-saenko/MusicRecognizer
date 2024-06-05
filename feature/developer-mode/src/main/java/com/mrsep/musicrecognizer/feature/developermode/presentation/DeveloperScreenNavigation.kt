package com.mrsep.musicrecognizer.feature.developermode.presentation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object DeveloperScreenNavigation {

    private const val ROUTE = "developer"

    fun NavGraphBuilder.developerScreen(
        onBackPressed: () -> Unit,
    ) {
        composable(ROUTE) {
            DeveloperScreen(onBackPressed = onBackPressed)
        }
    }

    fun NavController.navigateToDeveloperScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }
}
