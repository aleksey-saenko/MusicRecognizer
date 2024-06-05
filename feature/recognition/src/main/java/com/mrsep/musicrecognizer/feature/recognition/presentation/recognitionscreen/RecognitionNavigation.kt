package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object RecognitionScreen {

    const val ROUTE = "recognition"

    fun NavGraphBuilder.recognitionScreen(
        autostart: Boolean,
        onResetAutostart: () -> Unit,
        onNavigateToTrackScreen: (trackId: String, from: NavBackStackEntry) -> Unit,
        onNavigateToQueueScreen: (recognitionId: Int?, from: NavBackStackEntry) -> Unit,
        onNavigateToPreferencesScreen: (from: NavBackStackEntry) -> Unit
    ) {
        composable(ROUTE) { backStackEntry ->
            RecognitionScreen(
                autostart = autostart,
                onResetAutostart = onResetAutostart,
                onNavigateToTrackScreen = { trackId ->
                    onNavigateToTrackScreen(trackId, backStackEntry)
                },
                onNavigateToQueueScreen = { recognitionId ->
                    onNavigateToQueueScreen(recognitionId, backStackEntry)
                },
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
