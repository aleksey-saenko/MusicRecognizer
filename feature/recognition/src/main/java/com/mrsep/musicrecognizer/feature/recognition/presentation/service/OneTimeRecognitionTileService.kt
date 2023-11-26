package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.Manifest
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import com.mrsep.musicrecognizer.core.ui.isDarkUiMode
import com.mrsep.musicrecognizer.feature.recognition.domain.ScreenRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@AndroidEntryPoint
internal class OneTimeRecognitionTileService : TileService() {

    @Inject
    lateinit var recognitionInteractor: ScreenRecognitionInteractor

    private val isReadyToRecognize
        get() = recognitionInteractor.screenRecognitionStatus.value is RecognitionStatus.Ready

    override fun onCreate() {
        super.onCreate()
        requestListeningState(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile(isReadyToRecognize)
    }


    override fun onClick() {
        super.onClick()
        if (areRequiredPermissionsGranted()) {
            // api34 -> can't start foreground service without mediate activity
            // https://issuetracker.google.com/issues/299506164
            // startNotificationService(oneTimeRecognition = true)
            val mediateActivityIntent = Intent(
                this,
                NotificationServiceActivity::class.java
            ).apply {
//                addFlags(FLAG_ACTIVITY_NEW_TASK)
                addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
                action = NotificationServiceActivity.ACTION_ONE_TIME_RECOGNITION
            }
            val pendingIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(mediateActivityIntent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            if (Build.VERSION.SDK_INT >= 34) {
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(mediateActivityIntent)
            }
        } else {
            if (isLocked) {
                unlockAndRun { showPermissionsDialog() }
            } else {
                showPermissionsDialog()
            }
        }
    }

    private fun updateTile(isReadyToRecognize: Boolean) {
        qsTile.label = getString(
            if (isReadyToRecognize) {
                StringsR.string.quick_tile_recognize
            } else {
                StringsR.string.quick_tile_cancel_recognition
            }
        )
        qsTile.state = if (isReadyToRecognize) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        qsTile.updateTile()
    }

    private fun areRequiredPermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED)
        } else {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) ==
                    PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun showPermissionsDialog() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).setFlags(FLAG_ACTIVITY_NEW_TASK)
        val themeId = if (isDarkUiMode()) {
            android.R.style.Theme_DeviceDefault_Dialog_Alert
        } else {
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        }
        val dialogMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getString(StringsR.string.quick_tile_permissions_blocked_api33)
        } else {
            getString(StringsR.string.quick_tile_permissions_blocked)
        }
        val dialogBuilder = AlertDialog.Builder(this, themeId)
            .setTitle(getString(StringsR.string.permissions))
            .setMessage(dialogMessage)
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
        showDialog(dialogBuilder.create())
    }

    companion object {
        fun requestListeningState(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, OneTimeRecognitionTileService::class.java)
            )
        }
    }

}
