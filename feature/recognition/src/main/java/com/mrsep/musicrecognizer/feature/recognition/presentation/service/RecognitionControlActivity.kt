package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mrsep.musicrecognizer.core.ui.isDarkUiMode
import com.mrsep.musicrecognizer.core.strings.R as StringsR

/*
 * This transparent activity is used to request required permissions for widgets and tiles
 * Also it helps TileService to start foreground recognition service from background
 * https://issuetracker.google.com/issues/299506164
 */

class RecognitionControlActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        val denied = result.mapNotNull { (permission, isGranted) ->
            permission.takeIf { !isGranted }
        }
        when {
            denied.isEmpty() -> onLaunchRecognition()

            denied.any { permission -> !shouldShowRequestPermissionRationale(permission) } -> {
                showPermissionsBlockedDialog()
            }

            else -> finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent.action) {
            RecognitionControlService.ACTION_LAUNCH_RECOGNITION -> {
                val requiredPermissions = getRequiredPermissionsForRecognition()
                if (checkPermissionsGranted(requiredPermissions)) {
                    onLaunchRecognition()
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

    private fun onLaunchRecognition() {
        startForegroundService(
            Intent(this, RecognitionControlService::class.java)
                .setAction(RecognitionControlService.ACTION_LAUNCH_RECOGNITION)
                .putExtra(RecognitionControlService.KEY_FOREGROUND_REQUESTED, true)
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
