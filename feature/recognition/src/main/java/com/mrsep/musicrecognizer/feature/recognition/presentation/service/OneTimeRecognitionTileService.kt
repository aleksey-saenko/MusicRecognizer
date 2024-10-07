package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@AndroidEntryPoint
internal class OneTimeRecognitionTileService : TileService() {

    @Inject
    @WidgetStatusHolder
    lateinit var statusHolder: RecognitionStatusHolder

    private val isReadyToRecognize
        get() = statusHolder.status.value !is RecognitionStatus.Recognizing

    override fun onCreate() {
        super.onCreate()
        requestListeningState(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile(isReadyToRecognize)
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        // api34 -> can't start foreground service without mediate activity
        // https://issuetracker.google.com/issues/299506164
        val mediateActivityIntent = Intent(
            this,
            RecognitionControlActivity::class.java
        ).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            if (isReadyToRecognize) {
                action = RecognitionControlService.ACTION_LAUNCH_RECOGNITION
                putExtra(RecognitionControlService.KEY_FOREGROUND_REQUESTED, true)
            } else {
                action = RecognitionControlService.ACTION_CANCEL_RECOGNITION
            }
        }
        if (Build.VERSION.SDK_INT >= 34) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                mediateActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(mediateActivityIntent)
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

    companion object {
        fun requestListeningState(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, OneTimeRecognitionTileService::class.java)
            )
        }
    }
}
