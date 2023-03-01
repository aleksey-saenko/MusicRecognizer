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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mrsep.musicrecognizer.MusicRecognizerApp
import com.mrsep.musicrecognizer.presentation.screens.recently.recentlyScreen
import com.mrsep.musicrecognizer.presentation.screens.home.HOME_ROUTE
import com.mrsep.musicrecognizer.presentation.screens.home.homeScreen
import com.mrsep.musicrecognizer.presentation.screens.onboarding.ONBOARDING_ROUTE
import com.mrsep.musicrecognizer.presentation.screens.onboarding.onboardingScreen
import com.mrsep.musicrecognizer.presentation.screens.preferences.preferencesScreen
import com.mrsep.musicrecognizer.presentation.screens.track.navigateToTrackScreen
import com.mrsep.musicrecognizer.presentation.screens.track.trackScreen
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val musicRecognizerApp get() = application as MusicRecognizerApp
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val uiState by viewModel.uiStateStream.collectAsStateWithLifecycle()
            MusicRecognizerTheme(
                dynamicColor = isDynamicColorsEnabled(uiState)
            ) {
                Surface {
                    val systemUiController = rememberSystemUiController()
                    val useDarkIcons = !isSystemInDarkTheme()
                    SideEffect {
                        systemUiController.setStatusBarColor(
                            color = Color.Transparent,
                            darkIcons = useDarkIcons
                        )
                    }
                    AnimatedVisibility(visible = uiState is MainActivityUiState.Success) {
                        ApplicationNavigation(
                            onboardingCompleted = isOnboardingCompleted(uiState),
                            onOnboardingClose = { this@MainActivity.finish() }
                        )
                    }
                }
            }

        }
    }

}

fun isDynamicColorsEnabled(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> false
        is MainActivityUiState.Success -> uiState.userPreferences.dynamicColorsEnabled
    }
}

fun isOnboardingCompleted(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> false
        is MainActivityUiState.Success -> uiState.userPreferences.onboardingCompleted
    }
}

@Composable
fun ApplicationNavigation(
    onboardingCompleted: Boolean,
    onOnboardingClose: () -> Unit
) {
    val topNavController = rememberNavController()
    NavHost(
        navController = topNavController,
        startDestination = if (onboardingCompleted) BOTTOM_BAR_HOST_ROUTE else ONBOARDING_ROUTE
    ) {
        onboardingScreen(
            onOnboardingCompleted = { },
            onOnboardingClose = onOnboardingClose
        )
        bottomBarNavHost()
    }
}

const val BOTTOM_BAR_HOST_ROUTE = "bottom_bar_host"
fun NavGraphBuilder.bottomBarNavHost(
) {
    composable(BOTTOM_BAR_HOST_ROUTE) {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = { AppNavigationBar(navController = navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = HOME_ROUTE,
                modifier = Modifier.padding(innerPadding)
            ) {
                recentlyScreen(onTrackClick = { mbId ->
                    navController.navigateToTrackScreen(
                        mbId = mbId
                    )
                })
                homeScreen()
                preferencesScreen()
                trackScreen(onBackPressed = { navController.navigateUp() })
            }
        }
    }

}