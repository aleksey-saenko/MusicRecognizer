package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object AboutScreenNavigation {

    private const val ROUTE = "about"

    fun NavGraphBuilder.aboutScreen(onBackPressed: () -> Unit) {
        composable(ROUTE) {
            AboutScreen(onBackPressed = onBackPressed)
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
