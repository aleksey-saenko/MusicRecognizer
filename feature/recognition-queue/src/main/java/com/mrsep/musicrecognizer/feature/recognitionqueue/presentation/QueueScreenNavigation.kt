package com.mrsep.musicrecognizer.feature.recognitionqueue.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink

object RecognitionQueueScreen {

    private const val ROOT_DEEP_LINK = "app://mrsep.musicrecognizer.com"
    const val ROUTE = "queue"

    fun NavGraphBuilder.queueScreen(
        onBackPressed: () -> Unit,
        onNavigateToTrackScreen: (trackMbId: String) -> Unit
    ) {
        composable(
            route = ROUTE,
            deepLinks = listOf(navDeepLink {
                uriPattern = "$ROOT_DEEP_LINK/$ROUTE"
            })
        ) {
            QueueScreen(
                onBackPressed = onBackPressed,
                onNavigateToTrackScreen = onNavigateToTrackScreen
            )
        }
    }

    fun NavController.navigateToQueueScreen(
        navOptions: NavOptions? = null
    ) {
        this.navigate(route = ROUTE, navOptions = navOptions)
    }

    fun createDeepLink(): String {
        return "$ROOT_DEEP_LINK/$ROUTE"
    }

}

