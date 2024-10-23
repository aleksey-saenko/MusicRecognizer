package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mrsep.musicrecognizer.core.ui.isDarkUiMode
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioCaptureMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR

/**
 * This transparent activity is used to request required permissions and media projection token,
 * when recognition is requested from widgets, quick tiles, shortcuts, and notifications.
 * Also it helps TileService to start foreground recognition service from background, see
 * https://issuetracker.google.com/issues/299506164
 */
@AndroidEntryPoint
class RecognitionControlActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private lateinit var requestedAudioCaptureMode: AudioCaptureMode

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        val denied = result.mapNotNull { (permission, isGranted) ->
            permission.takeIf { !isGranted }
        }
        when {
            denied.isEmpty() -> onRecognitionPermissionsGranted()

            denied.any { permission -> !shouldShowRequestPermissionRationale(permission) } -> {
                showPermissionsBlockedDialog()
            }

            else -> finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestMediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) finish()
        result.data?.let { mediaProjectionData ->
            val mode = when (requestedAudioCaptureMode) {
                AudioCaptureMode.Microphone -> AudioCaptureServiceMode.Microphone
                AudioCaptureMode.Device -> AudioCaptureServiceMode.Device(mediaProjectionData)
                AudioCaptureMode.Auto -> AudioCaptureServiceMode.Auto(mediaProjectionData)
            }
            onLaunchRecognition(mode)
        } ?: finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            when (intent.action) {
                RecognitionControlService.ACTION_LAUNCH_RECOGNITION -> {
                    requestedAudioCaptureMode = preferencesRepository
                        .userPreferencesFlow.first()
                        .defaultAudioCaptureMode
                    val requiredPermissions = getRequiredPermissionsForRecognition()
                    if (checkPermissionsGranted(requiredPermissions)) {
                        onRecognitionPermissionsGranted()
                    } else {
                        val shouldShowRationale = requiredPermissions
                            .any { shouldShowRequestPermissionRationale(it) }
                        if (shouldShowRationale) {
                            showPermissionsRationaleDialog()
                        } else {
                            requestPermissionLauncher.launch(requiredPermissions)
                        }
                    }
                }

                RecognitionControlService.ACTION_CANCEL_RECOGNITION -> {
                    onCancelRecognition()
                }

                else -> {
                    finish()
                }
            }
        }
    }

    private fun onRecognitionPermissionsGranted() {
        when (requestedAudioCaptureMode) {
            AudioCaptureMode.Microphone -> onLaunchRecognition(AudioCaptureServiceMode.Microphone)
            AudioCaptureMode.Device,
            AudioCaptureMode.Auto -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = mediaProjectionManager.createScreenCaptureIntentForDisplay()
                requestMediaProjectionLauncher.launch(intent)
            } else {
                Log.w(this::class.java.simpleName, "AudioPlaybackCapture API is available on Android 10+")
                finish()
            }
        }
    }

    private fun onLaunchRecognition(audioCaptureServiceMode: AudioCaptureServiceMode) {
        startForegroundService(
            Intent(this, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_LAUNCH_RECOGNITION)
                .putExtra(RecognitionControlService.KEY_FOREGROUND_REQUESTED, true)
                .putExtra(RecognitionControlService.KEY_AUDIO_CAPTURE_SERVICE_MODE, audioCaptureServiceMode)
        )
        finish()
    }

    private fun onCancelRecognition() {
        startService(
            Intent(this, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_CANCEL_RECOGNITION)
        )
        finish()
    }

    private fun checkPermissionsGranted(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getRequiredPermissionsForRecognition(): Array<String> {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun getPermissionDialogTheme(): Int {
        return if (isDarkUiMode()) {
            android.R.style.Theme_DeviceDefault_Dialog_Alert
        } else {
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        }
    }

    private fun showPermissionsRationaleDialog() {
        var isDialogDismissListenerEnabled = true
        AlertDialog.Builder(this, getPermissionDialogTheme())
            .setTitle(StringsR.string.permissions)
            .setMessage(
                buildString {
                    append(getString(StringsR.string.permission_rationale_record_audio))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        append(" ")
                        append(getString(StringsR.string.permission_rationale_post_notifications))
                    }
                }
            )
            .setPositiveButton(getString(StringsR.string.request_permission)) { dialog, _ ->
                isDialogDismissListenerEnabled = false
                requestPermissionLauncher.launch(getRequiredPermissionsForRecognition())
                dialog.dismiss()
            }
            .setNegativeButton(getString(StringsR.string.not_now)) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                if (isDialogDismissListenerEnabled) finish()
            }
            .create()
            .show()
    }

    private fun showPermissionsBlockedDialog() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val dialogBuilder = AlertDialog.Builder(this, getPermissionDialogTheme())
            .setTitle(StringsR.string.permissions)
            .setMessage(
                buildString {
                    append(getString(StringsR.string.permission_rationale_record_audio))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        append(" ")
                        append(getString(StringsR.string.permission_rationale_post_notifications))
                    }
                    append("\n")
                    append(getString(StringsR.string.permissions_denied_message))
                }
            )
        if (appSettingsIntent.resolveActivity(packageManager) != null) {
            dialogBuilder
                .setPositiveButton(getString(StringsR.string.permissions_denied_open_settings)) { dialog, _ ->
                    startActivity(appSettingsIntent)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(StringsR.string.not_now)) { dialog, _ ->
                    dialog.dismiss()
                }
        } else {
            dialogBuilder
                .setPositiveButton(getString(StringsR.string.close)) { dialog, _ ->
                    dialog.dismiss()
                }
        }
        dialogBuilder
            .setOnDismissListener {
                finish()
            }
            .create()
            .show()
    }
}

internal fun MediaProjectionManager.createScreenCaptureIntentForDisplay(): Intent {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        createScreenCaptureIntent()
    } else {
        createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
    }
}
