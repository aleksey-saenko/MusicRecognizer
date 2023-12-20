package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object LyricsScreen {

    private const val ROOT_ROUTE = "lyrics"
    private const val ROOT_DEEP_LINK = "app://mrsep.musicrecognizer.com"
    private const val ARG_TRACK_ID = "trackId"

    const val ROUTE = "$ROOT_ROUTE/{$ARG_TRACK_ID}"

    data class Args(val trackId: String) {
        constructor(savedStateHandle: SavedStateHandle) : this(
            trackId = checkNotNull(savedStateHandle[ARG_TRACK_ID])
        )
    }

    private fun routeWithArgs(trackId: String) = "$ROOT_ROUTE/$trackId"

    fun NavGraphBuilder.lyricsScreen(
        onBackPressed: () -> Unit
    ) {
        composable(
            route = ROUTE,
            arguments = listOf(navArgument(ARG_TRACK_ID) { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "$ROOT_DEEP_LINK/$ROUTE"
            })
        ) { backStackEntry ->
            LyricsScreen(
                onBackPressed = onBackPressed
            )

        }
    }

    fun NavController.navigateToLyricsScreen(
        trackId: String,
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = routeWithArgs(trackId), navOptions = navOptions)
        }

    }

    fun createDeepLink(trackId: String): String {
        return "$ROOT_DEEP_LINK/${routeWithArgs(trackId)}"
    }

}