package com.mrsep.musicrecognizer.presentation

import android.content.Intent
import android.content.Intent.ACTION_MAIN
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mrsep.musicrecognizer.MusicRecognizerApp
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.domain.ThemeMode
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private var isServiceStartupHandled = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleRecognitionRequest(intent)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        if (savedInstanceState == null) {
            handleRecognitionRequest(intent)
        }
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = shouldUseDarkTheme(uiState)
            val recognitionRequested by viewModel.recognitionRequested.collectAsStateWithLifecycle()
            val unviewedTracksCount = viewModel.unviewedTracksCount.collectAsStateWithLifecycle()

            MusicRecognizerTheme(
                darkTheme = darkTheme,
                dynamicColor = shouldUseDynamicColors(uiState),
                pureBlack = shouldUsePureBlack(uiState)
            ) {
                // Update enableEdgeToEdge to match user theme mode
                DisposableEffect(darkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                        ) { darkTheme },
                        navigationBarStyle = if (darkTheme) {
                            SystemBarStyle.dark(Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                        }
                    )
                    onDispose {}
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        unviewedTracksCount = unviewedTracksCount,
                        recognitionRequested = recognitionRequested,
                        setRecognitionRequested = viewModel::setRecognitionRequested,
                        shouldShowNavRail = shouldShowNavRail(windowSizeClass),
                        isExpandedScreen = isExpandedScreen(windowSizeClass),
                        onboardingCompleted = isOnboardingCompleted(uiState),
                        onOnboardingClose = { this@MainActivity.finish() },
                        hideSplashScreen = { keepSplashScreen = false }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isServiceStartupHandled) {
            startNotificationServiceOnDemand()
        }
    }

    private fun handleRecognitionRequest(intent: Intent) {
        when (intent.action) {
            MusicRecognizerApp.ACTION_RECOGNIZE -> viewModel.setRecognitionRequested(true)
            ACTION_MAIN -> viewModel.requestRecognitionOnStartupIfPreferred()
        }
    }

    // Start previously started service if it was force killed for some reason
    private fun startNotificationServiceOnDemand() {
        lifecycleScope.launch {
            val shouldTurnOnService = viewModel.uiState
                .filterIsInstance<MainActivityUiState.Success>()
                .map { it.userPreferences.notificationServiceEnabled }
                .first()
            if (shouldTurnOnService) {
                startService(
                    Intent(
                        this@MainActivity,
                        NotificationService::class.java
                    ).apply {
                        action = NotificationService.HOLD_MODE_ON_ACTION
                        putExtra(NotificationService.KEY_RESTRICTED_START, false)
                    }
                )
            }
            isServiceStartupHandled = true
        }
    }
}

@Stable
private fun shouldUseDynamicColors(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> false
        is MainActivityUiState.Success -> uiState.userPreferences.dynamicColorsEnabled
    }
}

@Stable
private fun isOnboardingCompleted(uiState: MainActivityUiState): Boolean? {
    return when (uiState) {
        MainActivityUiState.Loading -> null
        is MainActivityUiState.Success -> uiState.userPreferences.onboardingCompleted
    }
}

@Stable
fun shouldShowBottomBar(windowSizeClass: WindowSizeClass) =
    windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

@Stable
fun shouldShowNavRail(windowSizeClass: WindowSizeClass) = !shouldShowBottomBar(windowSizeClass)

@Stable
fun isExpandedScreen(windowSizeClass: WindowSizeClass) =
    windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded ||
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

@Composable
private fun shouldUseDarkTheme(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()
    is MainActivityUiState.Success -> when (uiState.userPreferences.themeMode) {
        ThemeMode.FollowSystem -> isSystemInDarkTheme()
        ThemeMode.AlwaysLight -> false
        ThemeMode.AlwaysDark -> true
    }
}

@Stable
private fun shouldUsePureBlack(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> uiState.userPreferences.usePureBlackForDarkTheme
}
