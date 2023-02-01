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
import androidx.compose.ui.Modifier
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
                Scaffold(
                    bottomBar = { MainNavigationBar() }
                ) { paddings ->
                    MainScreen(
                        modifier = Modifier.padding(paddings)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        permissionContract.launch(Manifest.permission.RECORD_AUDIO)
    }


}