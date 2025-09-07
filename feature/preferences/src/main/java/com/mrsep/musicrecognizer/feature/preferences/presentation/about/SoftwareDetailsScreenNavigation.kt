package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object SoftwareDetailsScreen {

    private const val ROOT_ROUTE = "third_party_software_details"
    private const val ARG_UNIQUE_ID = "uniqueId"

    const val ROUTE = "$ROOT_ROUTE/{$ARG_UNIQUE_ID}"

    private fun routeWithArgs(uniqueId: String): String {
        return "$ROOT_ROUTE/$uniqueId"
    }

    fun NavGraphBuilder.softwareDetailsScreen(onBackPressed: () -> Unit) {
        composable(
            route = ROUTE,
            arguments = listOf(
                navArgument(ARG_UNIQUE_ID) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            SoftwareDetailsScreen(
                onBackPressed = onBackPressed,
                uniqueId = requireNotNull(backStackEntry.arguments?.getString(ARG_UNIQUE_ID))
            )
        }
    }

    fun NavController.navigateToSoftwareDetailsScreen(
        uniqueId: String,
        from: NavBackStackEntry,
        navOptions: NavOptions? = null,
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = routeWithArgs(uniqueId), navOptions = navOptions)
        }
    }
}
