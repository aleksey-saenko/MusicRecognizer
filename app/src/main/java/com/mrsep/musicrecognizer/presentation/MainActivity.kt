package com.mrsep.musicrecognizer.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mrsep.musicrecognizer.feature.library.presentation.LibraryScreen.libraryScreen
import com.mrsep.musicrecognizer.feature.onboarding.presentation.OnboardingScreen
import com.mrsep.musicrecognizer.feature.onboarding.presentation.OnboardingScreen.onboardingScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen.preferencesScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AboutScreenNavigation.aboutScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.about.AboutScreenNavigation.navigateToAboutScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.RecognitionScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.RecognitionScreen.recognitionScreen
import com.mrsep.musicrecognizer.presentation.navigationbar.NavigationBarCustom
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.toggleNotificationService
import com.mrsep.musicrecognizer.feature.recognitionqueue.presentation.RecognitionQueueScreen.navigateToQueueScreen
import com.mrsep.musicrecognizer.feature.recognitionqueue.presentation.RecognitionQueueScreen.queueScreen
import com.mrsep.musicrecognizer.feature.track.presentation.TrackScreen.navigateToTrackScreen
import com.mrsep.musicrecognizer.feature.track.presentation.TrackScreen.trackScreen
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //    private val musicRecognizerApp get() = application as MusicRecognizerApp
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setupApplicationWithPreferences()
        setContent {
            val uiState by viewModel.uiStateStream.collectAsStateWithLifecycle()
            MusicRecognizerTheme(
                dynamicColor = isDynamicColorsEnabled(uiState)
            ) {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }
                Surface(
                    color = Color.Unspecified,
                    contentColor = contentColorFor(MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.background
                        )
                        .systemBarsPadding(),
                ) {
                    AnimatedVisibility(
                        visible = uiState is MainActivityUiState.Success,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        AppNavigation(
                            onboardingCompleted = isOnboardingCompleted(uiState),
                            onOnboardingClose = { this@MainActivity.finish() }
                        )
                    }
                }
            }

        }
    }

    private fun setupApplicationWithPreferences() {
        lifecycleScope.launch {
            viewModel.uiStateStream.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .filterIsInstance<MainActivityUiState.Success>()
                .map { it.userPreferences }
                .onEach { userPreferences ->
                    toggleNotificationService(userPreferences.notificationServiceEnabled)
                }
                .launchIn(this)
        }
    }

}

private fun isDynamicColorsEnabled(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> false
        is MainActivityUiState.Success -> uiState.userPreferences.dynamicColorsEnabled
    }
}

private fun isOnboardingCompleted(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> false
        is MainActivityUiState.Success -> uiState.userPreferences.onboardingCompleted
    }
}

@Composable
private fun AppNavigation(
    onboardingCompleted: Boolean,
    onOnboardingClose: () -> Unit
) {
    val topNavController = rememberNavController()
//    printBackStack(topNavController, tag = "outer")
    NavHost(
        navController = topNavController,
        startDestination = if (onboardingCompleted) BOTTOM_BAR_HOST_ROUTE else OnboardingScreen.ROUTE
    ) {
        onboardingScreen(
            onOnboardingCompleted = { },
            onOnboardingClose = onOnboardingClose
        )
        bottomBarNavHost(
            topNavController = topNavController
        )
        trackScreen(
            onBackPressed = topNavController::navigateUp
        )
        queueScreen(
            onBackPressed = topNavController::navigateUp,
            onNavigateToTrackScreen = { mbId ->
                topNavController.navigateToTrackScreen(mbId = mbId)
            }
        )
        aboutScreen(onBackPressed = topNavController::navigateUp)
    }
}

private const val BOTTOM_BAR_HOST_ROUTE = "bottom_bar_host"

private fun NavGraphBuilder.bottomBarNavHost(
    topNavController: NavController
) {
    composable(BOTTOM_BAR_HOST_ROUTE) {
        val innerNavController = rememberNavController()
//        printBackStack(innerNavController, tag = "inner")
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavHost(
                navController = innerNavController,
                startDestination = RecognitionScreen.ROUTE,
                modifier = Modifier.weight(1f, false)
            ) {
                libraryScreen(onTrackClick = { mbId ->
                    topNavController.navigateToTrackScreen(mbId = mbId)
                })
                recognitionScreen(
                    onNavigateToTrackScreen = { mbId ->
                        topNavController.navigateToTrackScreen(mbId = mbId)
                    },
                    onNavigateToQueueScreen = {
                        topNavController.navigateToQueueScreen()
                    }
                )
                preferencesScreen(
                    onNavigateToAboutScreen = {
                        topNavController.navigateToAboutScreen()
                    },
                    onNavigateToQueueScreen = {
                        topNavController.navigateToQueueScreen()
                    }
                )
            }
            NavigationBarCustom(navController = innerNavController)
        }
    }

}