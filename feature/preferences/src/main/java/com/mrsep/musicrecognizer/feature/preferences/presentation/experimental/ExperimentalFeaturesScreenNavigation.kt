package com.mrsep.musicrecognizer.feature.preferences.presentation.experimental

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object ExperimentalFeaturesScreenNavigation {

    const val ROUTE = "preferences_experimental"

    fun NavGraphBuilder.experimentalFeaturesScreen(
        onBackPressed: () -> Unit
    ) {
        composable(ROUTE) { backStackEntry ->
            ExperimentalFeaturesScreen(
                onBackPressed = onBackPressed,
            )
        }
    }

    fun NavController.navigateToExperimentalFeaturesScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }
}