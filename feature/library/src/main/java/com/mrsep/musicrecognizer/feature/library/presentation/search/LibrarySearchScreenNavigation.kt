package com.mrsep.musicrecognizer.feature.library.presentation.search

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object LibrarySearchScreen {

    const val ROUTE = "library_search"

    fun NavGraphBuilder.librarySearchScreen(
        onBackPressed: () -> Unit,
        onTrackClick: (mbId: String, from: NavBackStackEntry) -> Unit
    ) {
        composable(ROUTE) { backStackEntry ->
            LibrarySearchScreen(
                onBackPressed = onBackPressed,
                onTrackClick = { mbId -> onTrackClick(mbId, backStackEntry) }
            )
        }
    }

    fun NavController.navigateToLibrarySearchScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }

}

