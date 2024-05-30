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

class NotificationServiceActivity : ComponentActivity() {

    private var isDialogDismissListenerEnabled = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        when {
            result.all { (_, isGranted) -> isGranted } -> onLaunchRecognition()
            result.any { (permission, _) -> !shouldShowRequestPermissionRationale(permission) } -> {
                showPermissionsBlockedDialog()
            }

            else -> finish()
        }
        isDialogDismissListenerEnabled = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent.action) {
            NotificationService.LAUNCH_RECOGNITION_ACTION -> {
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

            NotificationService.CANCEL_RECOGNITION_ACTION -> {
                onCancelRecognition()
            }

            else -> {
                finish()
            }
        }
    }


    private fun onLaunchRecognition() {
        startForegroundService(
            Intent(this, NotificationService::class.java)
                .setAction(NotificationService.LAUNCH_RECOGNITION_ACTION)
                .putExtra(NotificationService.KEY_FOREGROUND_REQUESTED, true)
        )
        finish()
    }

    private fun onCancelRecognition() {
        startService(
            Intent(this, NotificationServiceActivity::class.java)
                .setAction(NotificationService.CANCEL_RECOGNITION_ACTION)
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
        AlertDialog.Builder(this, getPermissionDialogTheme())
            .setTitle(StringsR.string.permissions)
            .setMessage(
                buildString {
                    append(getString(StringsR.string.recorder_permission_rationale_message))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        append(" ")
                        append(getString(StringsR.string.notificaiton_permission_rationale_message))
                    }
                }
            )
            .setPositiveButton(getString(StringsR.string.request_permission)) { _, _ ->
                isDialogDismissListenerEnabled = false
                requestPermissionLauncher.launch(getRequiredPermissionsForRecognition())
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
                    append(getString(StringsR.string.recorder_permission_rationale_message))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        append(" ")
                        append(getString(StringsR.string.notificaiton_permission_rationale_message))
                    }
                    append("\n")
                    append(getString(StringsR.string.permissions_denied_message))
                }
            )
        if (appSettingsIntent.resolveActivity(packageManager) != null) {
            dialogBuilder
                .setPositiveButton(getString(StringsR.string.open_settings)) { _, _ ->
                    startActivity(appSettingsIntent)
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