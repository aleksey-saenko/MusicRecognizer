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
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.MusicRecognizerApp
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.domain.ThemeMode
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.startNotificationService
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.stopNotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            setIntent(intent)
            handleRecognitionRequest(intent)
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        if (savedInstanceState == null) {
            handleRecognitionRequest(intent)
        }
        setupApplicationWithPreferences()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = shouldUseDarkTheme(uiState)
            val recognitionRequested by viewModel.recognitionRequested.collectAsStateWithLifecycle()
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
                        navigationBarStyle = SystemBarStyle.auto(
                            lightScrim,
                            darkScrim,
                        ) { darkTheme },
                    )
                    onDispose {}
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
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

    private fun handleRecognitionRequest(intent: Intent) {
        when (intent.action) {
            MusicRecognizerApp.ACTION_RECOGNIZE -> viewModel.setRecognitionRequested(true)
            ACTION_MAIN -> viewModel.requestRecognitionOnStartupIfPreferred()
        }
    }

    // TODO: remove from activity after fixing device boot case
    private fun setupApplicationWithPreferences() {
        lifecycleScope.launch {
            viewModel.uiState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .filterIsInstance<MainActivityUiState.Success>()
                .map { state -> state.userPreferences }
                .onEach { userPreferences ->
                    when (userPreferences.notificationServiceEnabled) {
                        true -> startNotificationService()
                        false -> stopNotificationService()
                    }
                }
                .launchIn(this)
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

private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)