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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.toggleNotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    @Inject lateinit var databaseFiller: DatabaseFiller
//    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    //    private val musicRecognizerApp get() = application as MusicRecognizerApp
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoadingState()
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setupApplicationWithPreferences()
        setContent {
            val uiState by viewModel.uiStateStream.collectAsStateWithLifecycle()
            MusicRecognizerTheme(
                dynamicColor = isDynamicColorsEnabled(uiState)
            ) {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                val statusBarColor = Color.Transparent
                val navigationBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = statusBarColor,
                        darkIcons = useDarkIcons
                    )
                    systemUiController.setNavigationBarColor(
                        color = navigationBarColor,
                        darkIcons = useDarkIcons
                    )
                }
                Surface(
                    color = Color.Unspecified,
                    contentColor = contentColorFor(MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
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

    override fun onStart() {
        super.onStart()
//        appScope.launch {
//            databaseFiller.prepopulateFromAssets(force = false)
//        }
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