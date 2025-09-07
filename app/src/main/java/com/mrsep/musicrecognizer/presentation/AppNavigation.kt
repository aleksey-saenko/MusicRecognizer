package com.mrsep.musicrecognizer.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.withResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.mrsep.musicrecognizer.BuildConfig
import com.mrsep.musicrecognizer.feature.backup.presentation.ExperimentalFeaturesScreenNavigation.experimentalFeaturesScreen
import com.mrsep.musicrecognizer.feature.backup.presentation.ExperimentalFeaturesScreenNavigation.navigateToExperimentalFeaturesScreen
import com.mrsep.musicrecognizer.feature.developermode.presentation.DeveloperScreenNavigation.developerScreen
import com.mrsep.musicrecognizer.feature.developermode.presentation.DeveloperScreenNavigation.navigateToDeveloperScreen
import com.mrsep.musicrecognizer.feature.library.presentation.library.LibraryScreen
import com.mrsep.musicrecognizer.feature.library.presentation.library.LibraryScreen.libraryScreen
import com.mrsep.musicrecognizer.feature.library.presentation.search.LibrarySearchScreen.librarySearchScreen
import com.mrsep.musicrecognizer.feature.library.presentation.search.LibrarySearchScreen.navigateToLibrarySearchScreen
import com.mrsep.musicrecognizer.feature.onboarding.presentation.OnboardingScreen
import com.mrsep.musicrecognizer.feature.onboarding.presentation.OnboardingScreen.onboardingScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen.navigateToPreferencesScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen.preferencesScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AboutScreenNavigation.aboutScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AboutScreenNavigation.navigateToAboutScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AppLicenseScreen.appLicenseScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AppLicenseScreen.navigateToAppLicenseScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.SoftwareDetailsScreen.softwareDetailsScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.SoftwareDetailsScreen.navigateToSoftwareDetailsScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.SoftwareScreenNavigation.softwareScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.SoftwareScreenNavigation.navigateToSoftwareScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen.RecognitionQueueScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen.RecognitionQueueScreen.navigateToQueueScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen.RecognitionQueueScreen.queueScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.RecognitionScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.RecognitionScreen.recognitionScreen
import com.mrsep.musicrecognizer.feature.track.presentation.lyrics.LyricsScreen.lyricsScreen
import com.mrsep.musicrecognizer.feature.track.presentation.lyrics.LyricsScreen.navigateToLyricsScreen
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackScreen
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackScreen.navigateToTrackScreen
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackScreen.trackScreen

private const val SCREEN_TRANSITION_DURATION = 250

@Composable
internal fun AppNavigation(
    unviewedTracksCount: State<Int>,
    recognitionRequested: Boolean,
    setRecognitionRequested: (Boolean) -> Unit,
    shouldShowNavRail: Boolean,
    isExpandedScreen: Boolean,
    onboardingCompleted: Boolean?,
    onOnboardingClose: () -> Unit,
    hideSplashScreen: () -> Unit
) {
    val outerNavController = rememberNavController()
    val innerNavController = rememberNavController()
    val optionalOuterEntry by outerNavController.currentBackStackEntryAsState()
    val optionalInnerEntry by innerNavController.currentBackStackEntryAsState()
    // Conditional navigation for onboarding and recognition request
    LaunchedEffect(
        optionalOuterEntry,
        optionalInnerEntry,
        onboardingCompleted,
        recognitionRequested
    ) {
        // optionalInnerEntry can be null, for example if nav graph is created with track deeplink
        val outerEntry = optionalOuterEntry ?: return@LaunchedEffect
        onboardingCompleted ?: return@LaunchedEffect
        val onOnboarding = outerEntry.destination.route == OnboardingScreen.ROUTE
        when {
            !onboardingCompleted && !onOnboarding -> {
                outerNavController.navigate(OnboardingScreen.ROUTE)
            }
            onboardingCompleted && onOnboarding -> {
                outerNavController.popBackStack()
            }
            else -> {
                // navigate to recognition screen if recognition requested
                if (recognitionRequested) {
                    if (outerEntry.destination.route != BAR_HOST_ROUTE) {
                        outerNavController.popBackStack(BAR_HOST_ROUTE, inclusive = false)
                    } else if (optionalInnerEntry?.destination?.route != RecognitionScreen.ROUTE) {
                        innerNavController.popBackStack(RecognitionScreen.ROUTE, inclusive = false)
                    }
                }
                // hide splash screen if onboarding navigation is finished and result screen is resumed
                if (outerEntry.destination.route == BAR_HOST_ROUTE) {
                    optionalInnerEntry?.lifecycle?.withResumed(hideSplashScreen)
                } else {
                    outerEntry.lifecycle.withResumed(hideSplashScreen)
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
        onboardingScreen(onOnboardingClose = onOnboardingClose)
        barNavHost(
            unviewedTracksCount = unviewedTracksCount,
            recognitionRequested = recognitionRequested,
            setRecognitionRequested = setRecognitionRequested,
            shouldShowNavRail = shouldShowNavRail,
            outerNavController = outerNavController,
            innerNavController = innerNavController
        )
        librarySearchScreen(
            onBackPressed = outerNavController::navigateUp,
            onTrackClick = { trackId, from ->
                outerNavController.navigateToTrackScreen(trackId = trackId, from = from)
            }
        )
        navigation(
            route = "nav_graph_track",
            startDestination = TrackScreen.ROUTE,
        ) {
            trackScreen(
                isExpandedScreen = isExpandedScreen,
                onBackPressed = outerNavController::navigateUp,
                onNavigateToLyricsScreen = { trackId, from ->
                    outerNavController.navigateToLyricsScreen(trackId = trackId, from = from)
                },
                onRetryRequested = { setRecognitionRequested(true) }
            )
            lyricsScreen(onBackPressed = outerNavController::navigateUp)
        }
        aboutScreen(
            onBackPressed = outerNavController::navigateUp,
            onNavigateToSoftwareScreen = { from ->
                outerNavController.navigateToSoftwareScreen(from)
            },
            onNavigateToAppLicenseScreen = { from ->
                outerNavController.navigateToAppLicenseScreen(from)
            }
        )
        softwareScreen(
            onBackPressed = outerNavController::navigateUp,
            onNavigateToSoftwareDetailsScreen = { uniqueId, from ->
                outerNavController.navigateToSoftwareDetailsScreen(uniqueId, from)
            }
        )
        softwareDetailsScreen(onBackPressed = outerNavController::navigateUp)
        appLicenseScreen(onBackPressed = outerNavController::navigateUp)
        experimentalFeaturesScreen(onBackPressed = outerNavController::navigateUp)
        developerScreen(onBackPressed = outerNavController::navigateUp)
    }
}

private const val BAR_HOST_ROUTE = "bar_host"

private fun NavGraphBuilder.barNavHost(
    unviewedTracksCount: State<Int>,
    recognitionRequested: Boolean,
    setRecognitionRequested: (Boolean) -> Unit,
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
                modifier = Modifier
                    .weight(1f, false)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                    )
            ) {
                if (shouldShowNavRail) {
                    AppNavigationRail(
                        unviewedTracksCount = unviewedTracksCount,
                        navController = innerNavController
                    )
                    VerticalDivider(modifier = Modifier.alpha(0.2f))
                }
                BarNavHost(
                    recognitionRequested = recognitionRequested,
                    setRecognitionRequested = setRecognitionRequested,
                    outerNavController = outerNavController,
                    innerNavController = innerNavController,
                    modifier = Modifier.weight(1f, false)
                )
            }
            if (!shouldShowNavRail) {
                AppNavigationBar(
                    unviewedTracksCount = unviewedTracksCount,
                    navController = innerNavController
                )
            }
        }
    }
}

@Composable
private fun BarNavHost(
    recognitionRequested: Boolean,
    setRecognitionRequested: (Boolean) -> Unit,
    outerNavController: NavController,
    innerNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = innerNavController,
        startDestination = TopLevelDestination.Recognition.route,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(animationSpec = tween(SCREEN_TRANSITION_DURATION)) },
    ) {
        navigation(
            startDestination = RecognitionScreen.ROUTE,
            route = TopLevelDestination.Recognition.route
        ) {
            recognitionScreen(
                autostart = recognitionRequested,
                onResetAutostart = { setRecognitionRequested(false) },
                onNavigateToTrackScreen = { trackId, from ->
                    outerNavController.navigateToTrackScreen(
                        trackId = trackId,
                        isRetryAvailable = true,
                        from = from
                    )
                },
                onNavigateToQueueScreen = { _, from ->
                    innerNavController.navigateToQueueScreen(
                        from = from,
                        navOptions = navOptions {
                            popUpTo(innerNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    )
                },
                onNavigateToPreferencesScreen = { from ->
                    innerNavController.navigateToPreferencesScreen(
                        from = from,
                        navOptions = navOptions {
                            popUpTo(innerNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    )
                }
            )
        }
        navigation(
            startDestination = LibraryScreen.ROUTE,
            route = TopLevelDestination.Library.route
        ) {
            libraryScreen(
                onTrackClick = { trackId, from ->
                    outerNavController.navigateToTrackScreen(trackId = trackId, from = from)
                },
                onTrackSearchClick = { from ->
                    outerNavController.navigateToLibrarySearchScreen(from = from)
                }
            )
        }
        navigation(
            startDestination = RecognitionQueueScreen.ROUTE,
            route = TopLevelDestination.Queue.route
        ) {
            queueScreen(
                onNavigateToTrackScreen = { trackId, from ->
                    outerNavController.navigateToTrackScreen(trackId = trackId, from = from)
                }
            )
        }
        navigation(
            startDestination = PreferencesScreen.ROUTE,
            route = TopLevelDestination.Preferences.route
        ) {
            preferencesScreen(
                showDeveloperOptions = BuildConfig.DEV_OPTIONS,
                onNavigateToAboutScreen = { from ->
                    outerNavController.navigateToAboutScreen(from)
                },
                onNavigateToExperimentalFeaturesScreen = { from ->
                    outerNavController.navigateToExperimentalFeaturesScreen(from)
                },
                onNavigateToDeveloperScreen = { from ->
                    outerNavController.navigateToDeveloperScreen(from)
                }
            )
        }
    }
}
