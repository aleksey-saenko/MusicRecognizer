package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object LibraryScreen {

    const val ROUTE = "library"

    fun NavGraphBuilder.libraryScreen(
        onTrackClick: (mbId: String, from: NavBackStackEntry) -> Unit,
        onTrackSearchClick: (from: NavBackStackEntry) -> Unit
    ) {
        composable(ROUTE) { backStackEntry ->
            LibraryScreen(
                onTrackClick = { mbId -> onTrackClick(mbId, backStackEntry) },
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


}

