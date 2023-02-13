package com.mrsep.musicrecognizer.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesRepository
import com.mrsep.musicrecognizer.presentation.screens.history.recentlyScreen
import com.mrsep.musicrecognizer.presentation.screens.home.homeScreen
import com.mrsep.musicrecognizer.presentation.screens.onboarding.OnboardingScreen
import com.mrsep.musicrecognizer.presentation.screens.onboarding.onboardingScreen
import com.mrsep.musicrecognizer.presentation.screens.preferences.preferencesScreen
import com.mrsep.musicrecognizer.presentation.screens.track.navigateToTrackScreen
import com.mrsep.musicrecognizer.presentation.screens.track.trackScreen
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private val permissionContract = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::onPermissionContractResult
    )

    private fun onPermissionContractResult(granted: Boolean) {
        Log.d("onPermissionContractResult", "permissions granted = $granted")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onboardingCompletedInitial = runBlocking {
            preferencesRepository.userPreferencesFlow.first().onboardingCompleted
        }

        setContent {
            var onboardingCompleted by remember { mutableStateOf(onboardingCompletedInitial) }
            MusicRecognizerTheme {
                if (onboardingCompleted) {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { AppNavigationBar(navController = navController) }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            recentlyScreen(onTrackClick = { mbId ->
                                navController.navigateToTrackScreen(
                                    mbId = mbId
                                )
                            })
                            homeScreen()
                            preferencesScreen()
                            onboardingScreen(
                                onSignUpClick = { link -> openUrlImplicitly(link) },
                                onApplyTokenClick = { }
                            )
                            trackScreen(onBackPressed = { navController.navigateUp() })
                        }
                    }
                } else {
                    Surface {
                        OnboardingScreen(
                            onSignUpClick = { link -> openUrlImplicitly(link) },
                            onApplyTokenClick = { onboardingCompleted = true }
                        )
                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        permissionContract.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun openUrlImplicitly(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Web browser not found", Toast.LENGTH_LONG).show()
        }
    }

}