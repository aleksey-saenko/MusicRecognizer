package com.mrsep.musicrecognizer.feature.library.presentation.library

import android.content.Intent
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object LibraryScreen {

    private const val ROOT_DEEP_LINK = "app://mrsep.musicrecognizer.com"
    const val ROUTE = "library"

    fun NavGraphBuilder.libraryScreen(
        onTrackClick: (trackId: String, from: NavBackStackEntry) -> Unit,
        onTrackSearchClick: (from: NavBackStackEntry) -> Unit
    ) {
        composable(
            route = ROUTE,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "$ROOT_DEEP_LINK/$ROUTE"
                    action = Intent.ACTION_VIEW
                }
            )
        ) { backStackEntry ->
            LibraryScreen(
                onTrackClick = { trackId -> onTrackClick(trackId, backStackEntry) },
                onTrackSearchClick = { onTrackSearchClick(backStackEntry) }
            )
        }
    }

    fun NavController.navigateToLibraryScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }

    fun createDeepLink(): String {
        return "$ROOT_DEEP_LINK/$ROUTE"
    }
}
