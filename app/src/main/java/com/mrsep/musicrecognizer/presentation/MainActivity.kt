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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesRepository
import com.mrsep.musicrecognizer.presentation.screens.home.HomeScreen
import com.mrsep.musicrecognizer.presentation.screens.onboarding.OnboardingScreen
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private val permissionContract = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::onPermissionContractResult
    )

//    private val viewModel by viewModels<HomeViewModel>()

    private fun onPermissionContractResult(granted: Boolean) {
        Log.d("onPermissionContractResult", "permissions granted = $granted")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            MusicRecognizerTheme {
                Scaffold(
                    bottomBar = { AppNavigationBar(navController = navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Destination.HOME.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Destination.HOME.route) { HomeScreen() }
                        composable(Destination.HISTORY.route) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) { Text(Destination.HISTORY.name) }
                        }
                        composable(Destination.SETTINGS.route) {
                            OnboardingScreen(
                                onSignUpClick = { link -> openUrlImplicitly(link) },
                                navController = navController,
                                modifier = Modifier
                            )
                        }
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