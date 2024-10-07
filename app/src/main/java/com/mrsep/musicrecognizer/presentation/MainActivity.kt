package com.mrsep.musicrecognizer.presentation

import android.content.ComponentName
import android.content.Context
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
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.domain.ThemeMode
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlService
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
            startControlServiceOnDemand()
        }
    }

    private fun handleRecognitionRequest(intent: Intent) {
        when (intent.action) {
            ACTION_RECOGNIZE -> viewModel.setRecognitionRequested(true)
            ACTION_MAIN -> {
                if (intent.getBooleanExtra(KEY_RESTART_ON_BACKUP_RESTORE, false)) return
                viewModel.requestRecognitionOnStartupIfPreferred()
            }
        }
    }

    // Start previously started service if it was force killed for some reason
    private fun startControlServiceOnDemand() {
        lifecycleScope.launch {
            val shouldTurnOnService = viewModel.uiState
                .filterIsInstance<MainActivityUiState.Success>()
                .map { it.userPreferences.notificationServiceEnabled }
                .first()
            if (shouldTurnOnService) {
                startService(
                    Intent(this@MainActivity, RecognitionControlService::class.java).apply {
                        action = RecognitionControlService.ACTION_HOLD_MODE_ON
                        putExtra(RecognitionControlService.KEY_RESTRICTED_START, false)
                    }
                )
            }
            isServiceStartupHandled = true
        }
    }

    companion object {
        const val ACTION_RECOGNIZE = "com.mrsep.musicrecognizer.intent.action.RECOGNIZE"
        private const val KEY_RESTART_ON_BACKUP_RESTORE = "RESTART_ON_BACKUP_RESTORE"

        fun Context.restartApplicationOnRestore() {
            val componentName = ComponentName(this, MainActivity::class.java)
            // Set the package explicitly, required for API 34 and later
            // Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
            val intent = Intent.makeRestartActivityTask(componentName)
                .setPackage(packageName)
                .putExtra(KEY_RESTART_ON_BACKUP_RESTORE, true)
            startActivity(intent)
            Runtime.getRuntime().exit(0)
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
