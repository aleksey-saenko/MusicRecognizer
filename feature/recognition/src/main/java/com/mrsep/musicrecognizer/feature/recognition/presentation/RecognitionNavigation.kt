package com.mrsep.musicrecognizer.feature.recognition.presentation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object RecognitionScreen {

    const val ROUTE = "recognition"

    fun NavGraphBuilder.recognitionScreen(
        onNavigateToTrackScreen: (mbId: String, from: NavBackStackEntry) -> Unit,
        onNavigateToQueueScreen: (enqueuedId: Int?, from: NavBackStackEntry) -> Unit,
        onNavigateToPreferencesScreen: (from: NavBackStackEntry) -> Unit
    ) {
        composable(ROUTE) { backStackEntry ->
            RecognitionScreen(
                onNavigateToTrackScreen = { mbId -> onNavigateToTrackScreen(mbId, backStackEntry) },
                onNavigateToQueueScreen = { enqueuedId -> onNavigateToQueueScreen(enqueuedId, backStackEntry) },
                onNavigateToPreferencesScreen = { onNavigateToPreferencesScreen(backStackEntry) }
            )
        }
    }

    fun NavController.navigateToRecognitionScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }

}