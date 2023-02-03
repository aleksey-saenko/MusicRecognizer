package com.mrsep.musicrecognizer.presentation

import android.Manifest
import android.os.Bundle
import android.util.Log
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
import com.mrsep.musicrecognizer.presentation.screens.home.HomeScreen
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionContract = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::onPermissionContractResult
    )

//    private val viewModel by viewModels<MainViewModel>()

    private fun onPermissionContractResult(granted: Boolean) {
        Log.d("onPermissionContractResult", "permissions granted = $granted")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicRecognizerTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { AppNavigationBar(navController = navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Destination.HOME.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Destination.HOME.route) { HomeScreen() }
                        composable(Destination.HISTORY.route) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text(Destination.HISTORY.name) }  }
                        composable(Destination.SETTINGS.route) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text(Destination.SETTINGS.name) }  }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        permissionContract.launch(Manifest.permission.RECORD_AUDIO)
    }


}