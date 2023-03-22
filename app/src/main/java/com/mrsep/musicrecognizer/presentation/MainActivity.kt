package com.mrsep.musicrecognizer.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mrsep.musicrecognizer.presentation.common.NavigationBarCustom
import com.mrsep.musicrecognizer.presentation.screens.library.libraryScreen
import com.mrsep.musicrecognizer.presentation.screens.home.HOME_ROUTE
import com.mrsep.musicrecognizer.presentation.screens.home.homeScreen
import com.mrsep.musicrecognizer.presentation.screens.onboarding.ONBOARDING_ROUTE
import com.mrsep.musicrecognizer.presentation.screens.onboarding.onboardingScreen
import com.mrsep.musicrecognizer.presentation.screens.preferences.preferencesScreen
import com.mrsep.musicrecognizer.presentation.screens.track.Screen.Track.navigateToTrackScreen
import com.mrsep.musicrecognizer.presentation.screens.track.Screen.Track.trackScreen
import com.mrsep.musicrecognizer.service.NotificationService.Companion.toggleNotificationService
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
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                ) {
                    val systemUiController = rememberSystemUiController()
                    val useDarkIcons = !isSystemInDarkTheme()
                    val backgroundColor = MaterialTheme.colorScheme.background
                    SideEffect {
                        systemUiController.setStatusBarColor(
                            color = backgroundColor,
                            darkIcons = useDarkIcons
                        )
                    }
                    AnimatedVisibility(visible = uiState is MainActivityUiState.Success) {
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
        startDestination = if (onboardingCompleted) BOTTOM_BAR_HOST_ROUTE else ONBOARDING_ROUTE
    ) {
        onboardingScreen(
            onOnboardingCompleted = { },
            onOnboardingClose = onOnboardingClose
        )
        trackScreen(
            onBackPressed = topNavController::navigateUp
        )
        bottomBarNavHost(
            topNavController = topNavController
        )
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
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavHost(
                navController = innerNavController,
                startDestination = HOME_ROUTE,
                modifier = Modifier.weight(1f, false)
            ) {
                libraryScreen(onTrackClick = { mbId ->
                    topNavController.navigateToTrackScreen(mbId = mbId)
                })
                homeScreen(
                    onNavigateToTrackScreen = { mbId ->
                        topNavController.navigateToTrackScreen(mbId = mbId)
                    }
                )
                preferencesScreen(navController = innerNavController)
            }
            NavigationBarCustom(navController = innerNavController)
        }
    }

}