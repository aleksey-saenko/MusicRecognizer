package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object AppLicenseScreen {

    const val ROUTE = "app_license"

    fun NavGraphBuilder.appLicenseScreen(onBackPressed: () -> Unit) {
        composable(route = ROUTE) {
            AppLicenseScreen(onBackPressed = onBackPressed)
        }
    }

    fun NavController.navigateToAppLicenseScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null,
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }
}
