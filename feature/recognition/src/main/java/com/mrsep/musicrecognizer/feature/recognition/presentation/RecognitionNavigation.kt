package com.mrsep.musicrecognizer.feature.recognition.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object RecognitionScreen {

    const val ROUTE = "recognition"

    fun NavGraphBuilder.recognitionScreen(
        onNavigateToTrackScreen: (mbId: String) -> Unit,
        onNavigateToQueueScreen: () -> Unit
    ) {
        composable(ROUTE) {
            RecognitionScreen(
                onNavigateToTrackScreen = onNavigateToTrackScreen,
                onNavigateToQueueScreen = onNavigateToQueueScreen
            )
        }
    }

    fun NavController.navigateToRecognitionScreen(
        navOptions: NavOptions? = null
    ) {
        this.navigate(route = ROUTE, navOptions = navOptions)
    }

}