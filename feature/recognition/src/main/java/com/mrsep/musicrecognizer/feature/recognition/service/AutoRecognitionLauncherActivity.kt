package com.mrsep.musicrecognizer.feature.recognition.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

private const val TAG = "AutoRecognitionLauncher"

/**
 * Transparent activity that brings the app to the foreground before starting
 * AutoRecognitionService. This is required on Android 14+ because foreground
 * services with microphone type cannot start from the background.
 */
class AutoRecognitionLauncherActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startAutoRecognitionService()
        } else {
            Log.w(TAG, "RECORD_AUDIO permission denied")
        }
        finish()
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        when (intent?.action) {
            ACTION_START -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                    startAutoRecognitionService()
                    finish()
                    overridePendingTransition(0, 0)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            ACTION_STOP -> {
                AutoRecognitionService.stop(applicationContext)
                finish()
                overridePendingTransition(0, 0)
            }
            else -> {
                finish()
                overridePendingTransition(0, 0)
            }
        }
    }

    private fun startAutoRecognitionService() {
        AutoRecognitionService.start(applicationContext)
    }

    companion object {
        const val ACTION_START = "com.mrsep.musicrecognizer.auto_recognize.START"
        const val ACTION_STOP = "com.mrsep.musicrecognizer.auto_recognize.STOP"

        fun startIntent(context: Context): Intent {
            return Intent(context, AutoRecognitionLauncherActivity::class.java)
                .setAction(ACTION_START)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, AutoRecognitionLauncherActivity::class.java)
                .setAction(ACTION_STOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
