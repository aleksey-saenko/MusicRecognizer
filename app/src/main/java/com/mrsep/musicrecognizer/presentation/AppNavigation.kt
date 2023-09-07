package com.mrsep.musicrecognizer.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.withResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mrsep.musicrecognizer.feature.developermode.presentation.DeveloperScreenNavigation.developerScreen
import com.mrsep.musicrecognizer.feature.developermode.presentation.DeveloperScreenNavigation.navigateToDeveloperScreen
import com.mrsep.musicrecognizer.feature.library.presentation.library.LibraryScreen.libraryScreen
import com.mrsep.musicrecognizer.feature.library.presentation.search.LibrarySearchScreen.librarySearchScreen
import com.mrsep.musicrecognizer.feature.library.presentation.search.LibrarySearchScreen.navigateToLibrarySearchScreen
import com.mrsep.musicrecognizer.feature.onboarding.presentation.OnboardingScreen
import com.mrsep.musicrecognizer.feature.onboarding.presentation.OnboardingScreen.onboardingScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen.navigateToPreferencesScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen.preferencesScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AboutScreenNavigation.aboutScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AboutScreenNavigation.navigateToAboutScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.RecognitionScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.RecognitionScreen.recognitionScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen.RecognitionQueueScreen.navigateToQueueScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen.RecognitionQueueScreen.queueScreen
import com.mrsep.musicrecognizer.feature.track.presentation.lyrics.LyricsScreen.lyricsScreen
import com.mrsep.musicrecognizer.feature.track.presentation.lyrics.LyricsScreen.navigateToLyricsScreen
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackScreen.navigateToTrackScreen
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackScreen.trackScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

private const val SCREEN_TRANSITION_DURATION = 300

@Composable
internal fun AppNavigation(
    shouldShowNavRail: Boolean,
    isExpandedScreen: Boolean,
    onboardingCompleted: Boolean?,
    onOnboardingClose: () -> Unit,
    hideSplashScreen: () -> Unit
) {
    val outerNavController = rememberNavController()
    val innerNavController = rememberNavController()

    val currentOuterEntry by outerNavController.currentBackStackEntryAsState()
    val currentInnerEntry by innerNavController.currentBackStackEntryAsState()
    // Conditional navigation for onboarding screen
    LaunchedEffect(onboardingCompleted, currentOuterEntry, currentInnerEntry) {
        val outerEntry = currentOuterEntry ?: return@LaunchedEffect
        onboardingCompleted ?: return@LaunchedEffect
        val onOnboarding = outerEntry.destination.route == OnboardingScreen.ROUTE
        when {
            !onboardingCompleted && !onOnboarding -> {
                outerNavController.navigate(OnboardingScreen.ROUTE)
            }
            onboardingCompleted && onOnboarding -> {
                outerNavController.popBackStack()
            }
            // hide splash screen if navigation is finished and result screen is resumed
            else -> {
                select<Unit> {
                    listOfNotNull(currentOuterEntry, outerEntry).forEach { entry ->
                        launch { entry.lifecycle.withResumed(hideSplashScreen) }.onJoin
                    }
                }
            }
        }
    }

    NavHost(
        navController = outerNavController,
        startDestination = BAR_HOST_ROUTE,
        enterTransition = { fadeIn(animationSpec = tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(animationSpec = tween(SCREEN_TRANSITION_DURATION)) },
    ) {
        onboardingScreen(
            onOnboardingCompleted = { },
            onOnboardingClose = onOnboardingClose
        )
        barNavHost(
            shouldShowNavRail = shouldShowNavRail,
            outerNavController = outerNavController,
            innerNavController = innerNavController
        )
        trackScreen(
            isExpandedScreen = isExpandedScreen,
            onBackPressed = outerNavController::navigateUp,
            onNavigateToLyricsScreen = { mbId, from ->
                outerNavController.navigateToLyricsScreen(mbId = mbId, from = from)
            }
        )
        lyricsScreen(
            onBackPressed = outerNavController::navigateUp
        )
        queueScreen(
            onBackPressed = outerNavController::navigateUp,
            onNavigateToTrackScreen = { mbId, from ->
                outerNavController.navigateToTrackScreen(mbId = mbId, from = from)
            }
        )
        aboutScreen(onBackPressed = outerNavController::navigateUp)
    }
}

private const val BAR_HOST_ROUTE = "bar_host"

private fun NavGraphBuilder.barNavHost(
    shouldShowNavRail: Boolean,
    outerNavController: NavController,
    innerNavController: NavHostController
) {
    composable(BAR_HOST_ROUTE) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.weight(1f, false)
            ) {
                if (shouldShowNavRail) AppNavigationRail(navController = innerNavController)
                BarNavHost(
                    outerNavController = outerNavController,
                    innerNavController = innerNavController,
                    modifier = Modifier.weight(1f, false)
                )
            }
            if (!shouldShowNavRail) AppNavigationBar(navController = innerNavController)
        }
    }
}

@Composable
private fun BarNavHost(
    outerNavController: NavController,
    innerNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = innerNavController,
        startDestination = RecognitionScreen.ROUTE,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(animationSpec = tween(SCREEN_TRANSITION_DURATION)) },
    ) {
        libraryScreen(
            onTrackClick = { mbId, from ->
                outerNavController.navigateToTrackScreen(mbId = mbId, from = from)
            },
            onTrackSearchClick = { from ->
                innerNavController.navigateToLibrarySearchScreen(from = from)
            }
        )
        librarySearchScreen(
            onBackPressed = innerNavController::navigateUp,
            onTrackClick = { mbId, from ->
                outerNavController.navigateToTrackScreen(mbId = mbId, from = from)
            }
        )
        recognitionScreen(
            onNavigateToTrackScreen = { mbId, from ->
                outerNavController.navigateToTrackScreen(mbId = mbId, from = from)
            },
            //TODO: implement navigation with highlighting enqueuedId
            onNavigateToQueueScreen = { enqueuedId, from ->
                outerNavController.navigateToQueueScreen(from = from)
            },
            onNavigateToPreferencesScreen = { from ->
                innerNavController.navigateToPreferencesScreen(from)
            }
        )
        preferencesScreen(
            showDeveloperOptions = true, //BuildConfig.DEBUG
            onNavigateToAboutScreen = { from ->
                outerNavController.navigateToAboutScreen(from)
            },
            onNavigateToQueueScreen = { from ->
                outerNavController.navigateToQueueScreen(from)
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