package com.mrsep.musicrecognizer.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.data.track.util.DatabaseFiller
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.toggleNotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseFiller: DatabaseFiller

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    private val viewModel: MainActivityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val keepSplashScreen = mutableStateOf(true)
        splashScreen.setKeepOnScreenCondition {
            keepSplashScreen.value
        }
        setupApplicationWithPreferences()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val uiState by viewModel.uiStateStream.collectAsStateWithLifecycle()
            MusicRecognizerTheme(
                dynamicColor = isDynamicColorsEnabled(uiState)
            ) {
                Surface(
                    color = Color.Unspecified,
                    contentColor = contentColorFor(MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                        .systemBarsPadding(),
                ) {
                    AppNavigation(
                        shouldShowNavRail = shouldShowNavRail(windowSizeClass),
                        isExpandedScreen = isExpandedScreen(windowSizeClass),
                        onboardingCompleted = isOnboardingCompleted(uiState),
                        onOnboardingClose = { this@MainActivity.finish() },
                        hideSplashScreen = { keepSplashScreen.value = false }
                    )
                }
            }
        }
    }

    private fun setupApplicationWithPreferences() {
        lifecycleScope.launch {
            viewModel.uiStateStream.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .filterIsInstance<MainActivityUiState.Success>()
                .map { state -> state.userPreferences }
                .onEach { userPreferences ->
                    toggleNotificationService(userPreferences.notificationServiceEnabled)
                }
                .launchIn(this)
        }
    }

}

@Stable
private fun isDynamicColorsEnabled(uiState: MainActivityUiState): Boolean {
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