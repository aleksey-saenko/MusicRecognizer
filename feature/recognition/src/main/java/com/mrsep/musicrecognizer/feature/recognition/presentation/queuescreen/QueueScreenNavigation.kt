package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import android.content.Intent
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object RecognitionQueueScreen {

    private const val ROOT_DEEP_LINK = "app://mrsep.musicrecognizer.com"
    const val ROUTE = "queue"

    fun NavGraphBuilder.queueScreen(
        onNavigateToTrackScreen: (trackId: String, from: NavBackStackEntry) -> Unit
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
            QueueScreen(
                onNavigateToTrackScreen = { trackId ->
                    onNavigateToTrackScreen(trackId, backStackEntry)
                }
            )
        }
    }

    fun NavController.navigateToQueueScreen(
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
