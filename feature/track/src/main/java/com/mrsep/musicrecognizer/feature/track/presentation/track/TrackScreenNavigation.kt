package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
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

object TrackScreen {

    private const val ROOT_ROUTE = "track"
    private const val ROOT_DEEP_LINK = "app://mrsep.musicrecognizer.com"
    private const val ARG_TRACK_ID = "trackId"
    private const val ARG_ALLOW_RETRY = "allowRetry"

    private const val KEY_LAST_ACTION = "KEY_LAST_ACTION"

    const val ROUTE = "$ROOT_ROUTE/{$ARG_TRACK_ID}?$ARG_ALLOW_RETRY={$ARG_ALLOW_RETRY}"

    data class Args(val trackId: String) {
        constructor(savedStateHandle: SavedStateHandle) : this(
            trackId = checkNotNull(savedStateHandle[ARG_TRACK_ID])
        )
    }

    private fun routeWithArgs(
        trackId: String,
        isRetryAvailable: Boolean
    ): String {
        return "$ROOT_ROUTE/$trackId?$ARG_ALLOW_RETRY=$isRetryAvailable"
    }

    fun NavGraphBuilder.trackScreen(
        isExpandedScreen: Boolean,
        onBackPressed: () -> Unit,
        onNavigateToLyricsScreen: (trackId: String, from: NavBackStackEntry) -> Unit,
        onRetryRequested: () -> Unit
    ) {
        composable(
            route = ROUTE,
            arguments = listOf(
                navArgument(ARG_TRACK_ID) { type = NavType.StringType },
                navArgument(ARG_ALLOW_RETRY) { type = NavType.BoolType; defaultValue = false },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "$ROOT_DEEP_LINK/$ROUTE"
                }
            ),
            popExitTransition = {
                when (initialState.savedStateHandle[KEY_LAST_ACTION] ?: TrackAction.NoAction) {
                    TrackAction.Deleted -> fadeOut(
                        animationSpec = tween(durationMillis = 250, delayMillis = 50)
                    ) + scaleOut(
                        animationSpec = tween(durationMillis = 400),
                        targetScale = 0.85f
                    )

                    TrackAction.Dismissed -> slideOutVertically(
                        animationSpec = tween(durationMillis = 300),
                        targetOffsetY = { fullHeight -> -fullHeight / 2 }
                    ) + fadeOut(tween(durationMillis = 250))

                    TrackAction.NoAction -> null
                }
            }
        ) { backStackEntry ->
            TrackScreen(
                isExpandedScreen = isExpandedScreen,
                isRetryAllowed = backStackEntry.arguments?.getBoolean(ARG_ALLOW_RETRY) ?: false,
                onBackPressed = onBackPressed,
                onNavigateToLyricsScreen = { trackId ->
                    onNavigateToLyricsScreen(trackId, backStackEntry)
                },
                onRetryRequested = {
                    backStackEntry.savedStateHandle[KEY_LAST_ACTION] = TrackAction.Dismissed
                    onRetryRequested()
                    onBackPressed()
                },
                onTrackDeleted = {
                    backStackEntry.savedStateHandle[KEY_LAST_ACTION] = TrackAction.Deleted
                    onBackPressed()
                }
            )
        }
    }

    fun NavController.navigateToTrackScreen(
        trackId: String,
        isRetryAvailable: Boolean = false,
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = routeWithArgs(trackId, isRetryAvailable), navOptions = navOptions)
        }
    }

    fun createDeepLink(trackId: String, isRetryAvailable: Boolean = false): String {
        return "$ROOT_DEEP_LINK/${routeWithArgs(trackId, isRetryAvailable)}"
    }
}

private enum class TrackAction { NoAction, Deleted, Dismissed }
