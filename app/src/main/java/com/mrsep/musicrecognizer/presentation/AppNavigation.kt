package com.mrsep.musicrecognizer.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mrsep.musicrecognizer.feature.developermode.presentation.DeveloperScreenNavigation.developerScreen
import com.mrsep.musicrecognizer.feature.developermode.presentation.DeveloperScreenNavigation.navigateToDeveloperScreen
import com.mrsep.musicrecognizer.feature.library.presentation.LibraryScreen.libraryScreen
import com.mrsep.musicrecognizer.feature.onboarding.presentation.OnboardingScreen
import com.mrsep.musicrecognizer.feature.onboarding.presentation.OnboardingScreen.onboardingScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen.navigateToPreferencesScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen.preferencesScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AboutScreenNavigation.aboutScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AboutScreenNavigation.navigateToAboutScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.RecognitionScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.RecognitionScreen.recognitionScreen
import com.mrsep.musicrecognizer.feature.recognitionqueue.presentation.RecognitionQueueScreen.navigateToQueueScreen
import com.mrsep.musicrecognizer.feature.recognitionqueue.presentation.RecognitionQueueScreen.queueScreen
import com.mrsep.musicrecognizer.feature.track.presentation.lyrics.LyricsScreen.lyricsScreen
import com.mrsep.musicrecognizer.feature.track.presentation.lyrics.LyricsScreen.navigateToLyricsScreen
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackScreen.navigateToTrackScreen
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackScreen.trackScreen

@Composable
internal fun AppNavigation(
    shouldShowNavRail: Boolean,
    isExpandedScreen: Boolean,
    onboardingCompleted: Boolean,
    onOnboardingClose: () -> Unit
) {
    val topNavController = rememberNavController()
    NavHost(
        navController = topNavController,
        startDestination = if (onboardingCompleted) BAR_HOST_ROUTE else OnboardingScreen.ROUTE
    ) {
        onboardingScreen(
            onOnboardingCompleted = { },
            onOnboardingClose = onOnboardingClose
        )
        barNavHost(
            shouldShowNavRail = shouldShowNavRail,
            topNavController = topNavController
        )
        trackScreen(
            isExpandedScreen = isExpandedScreen,
            onBackPressed = topNavController::navigateUp,
            onNavigateToLyricsScreen = { mbId, from ->
                topNavController.navigateToLyricsScreen(mbId = mbId, from = from)
            }
        )
        lyricsScreen(
            onBackPressed = topNavController::navigateUp
        )
        queueScreen(
            onBackPressed = topNavController::navigateUp,
            onNavigateToTrackScreen = { mbId, from ->
                topNavController.navigateToTrackScreen(mbId = mbId, from = from)
            }
        )
        aboutScreen(onBackPressed = topNavController::navigateUp)
    }
}

private const val BAR_HOST_ROUTE = "bar_host"

private fun NavGraphBuilder.barNavHost(
    shouldShowNavRail: Boolean,
    topNavController: NavController
) {
    composable(BAR_HOST_ROUTE) {
        val innerNavController = rememberNavController()
        if (shouldShowNavRail) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                AppNavigationRail(navController = innerNavController)
                BarNavHost(
                    topNavController = topNavController,
                    innerNavController = innerNavController,
                    modifier = Modifier.weight(1f, false)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BarNavHost(
                    topNavController = topNavController,
                    innerNavController = innerNavController,
                    modifier = Modifier.weight(1f, false)
                )
                AppNavigationBar(navController = innerNavController)
            }
        }
    }
}

@Composable
private fun BarNavHost(
    topNavController: NavController,
    innerNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = innerNavController,
        startDestination = RecognitionScreen.ROUTE,
        modifier = modifier
    ) {
        libraryScreen(onTrackClick = { mbId, from ->
            topNavController.navigateToTrackScreen(mbId = mbId, from = from)
        })
        recognitionScreen(
            onNavigateToTrackScreen = { mbId, from ->
                topNavController.navigateToTrackScreen(mbId = mbId, from = from)
            },
            //TODO: implement navigation with highlighting enqueuedId
            onNavigateToQueueScreen = { enqueuedId, from ->
                topNavController.navigateToQueueScreen(from = from)
            },
            onNavigateToPreferencesScreen = { from ->
                innerNavController.navigateToPreferencesScreen(from)
            }
        )
        preferencesScreen(
            onNavigateToAboutScreen = { from ->
                topNavController.navigateToAboutScreen(from)
            },
            onNavigateToQueueScreen = { from ->
                topNavController.navigateToQueueScreen(from)
            },
            onNavigateToDeveloperScreen = { from ->
                innerNavController.navigateToDeveloperScreen(from)
            }
        )
        developerScreen(
            onBackPressed = innerNavController::navigateUp
        )
    }
}