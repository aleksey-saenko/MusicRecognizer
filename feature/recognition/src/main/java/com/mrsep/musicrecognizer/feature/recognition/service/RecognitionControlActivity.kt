package com.mrsep.musicrecognizer.feature.recognition.service

import android.Manifest
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.ui.isDarkUiMode
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
    private var useAltDeviceSoundSource = false

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
            val mode = requestedAudioCaptureMode.toServiceMode(mediaProjectionData)
            onLaunchRecognition(mode)
        } ?: finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enforceTransparentEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        when (intent.action) {
            ACTION_LAUNCH_RECOGNITION_WITH_PERMISSIONS_REQUEST -> lifecycleScope.launch {
                val preferences = preferencesRepository.userPreferencesFlow.first()
                requestedAudioCaptureMode = preferences.defaultAudioCaptureMode
                useAltDeviceSoundSource = preferences.useAltDeviceSoundSource
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

            else -> error("Unknown intent action")
        }
    }

    private fun onRecognitionPermissionsGranted() {
        when (requestedAudioCaptureMode) {
            AudioCaptureMode.Microphone -> {
                onLaunchRecognition(requestedAudioCaptureMode.toServiceMode(null))
            }
            AudioCaptureMode.Device,
            AudioCaptureMode.Auto -> if (useAltDeviceSoundSource) {
                onLaunchRecognition(requestedAudioCaptureMode.toServiceMode(null))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = mediaProjectionManager.createScreenCaptureIntentForDisplay()
                requestMediaProjectionLauncher.launch(intent)
            } else {
                Log.w(this::class.java.simpleName, "AudioPlaybackCapture API is available on Android 10+")
                finish()
            }
        }
    }

    private fun onLaunchRecognition(audioCaptureServiceMode: AudioCaptureServiceMode) {
        RecognitionControlService.startRecognition(this.applicationContext, audioCaptureServiceMode)
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
        ).setFlags(FLAG_ACTIVITY_NEW_TASK)
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

    @Suppress("DEPRECATION")
    private fun enforceTransparentEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
    }

    companion object {
        private const val ACTION_LAUNCH_RECOGNITION_WITH_PERMISSIONS_REQUEST = "com.mrsep.musicrecognizer.control_activity.action.launch_recognition_permissions"

        fun startRecognitionWithPermissionRequestIntent(context: Context): Intent {
            return Intent(context, RecognitionControlActivity::class.java)
                .setAction(ACTION_LAUNCH_RECOGNITION_WITH_PERMISSIONS_REQUEST)
                .addFlags(FLAG_ACTIVITY_NEW_TASK)
        }

        fun startRecognitionWithPermissionRequestPendingIntent(context: Context): PendingIntent {
            return PendingIntent.getActivity(
                context,
                0,
                startRecognitionWithPermissionRequestIntent(context),
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

internal fun MediaProjectionManager.createScreenCaptureIntentForDisplay(): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
    } else {
        createScreenCaptureIntent()
    }
}

internal fun AudioCaptureMode.toServiceMode(mediaProjectionData: Intent?) = when (this) {
    AudioCaptureMode.Microphone -> AudioCaptureServiceMode.Microphone
    AudioCaptureMode.Device -> AudioCaptureServiceMode.Device(mediaProjectionData)
    AudioCaptureMode.Auto -> AudioCaptureServiceMode.Auto(mediaProjectionData)
}
