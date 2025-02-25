package com.mrsep.musicrecognizer.feature.recognition.service

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.RecognitionStatusHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR

private const val TAG = "OneTimeRecognitionTileService"

@AndroidEntryPoint
class OneTimeRecognitionTileService : TileService() {

    private var coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tileUpdateJob: Job? = null

    @Inject
    @WidgetStatusHolder
    internal lateinit var statusHolder: RecognitionStatusHolder

    private val appContext get() = applicationContext

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        requestListeningState(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        tileUpdateJob = coroutineScope.launch {
            statusHolder.status.collect { status -> updateTile(status.isReadyToRecognize) }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        tileUpdateJob?.cancel()
        tileUpdateJob = null
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        // api34 -> can't start foreground service without mediate activity
        // https://issuetracker.google.com/issues/299506164
        if (statusHolder.status.value.isReadyToRecognize) {
            if (Build.VERSION.SDK_INT >= 34) {
                startActivityAndCollapse(
                    RecognitionControlActivity.startRecognitionWithPermissionRequestPendingIntent(appContext)
                )
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(
                    RecognitionControlActivity.startRecognitionWithPermissionRequestIntent(appContext)
                )
            }
        } else {
            RecognitionControlService.cancelRecognition(appContext)
        }
    }

    private fun updateTile(isReadyToRecognize: Boolean) {
        val tile = qsTile ?: run {
            Log.e(TAG, "qsTile is null, is update called in valid state?")
            return
        }
        tile.label = getString(
            if (isReadyToRecognize) {
                StringsR.string.quick_tile_recognize
            } else {
                StringsR.string.quick_tile_cancel_recognition
            }
        )
        tile.state = if (isReadyToRecognize) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.updateTile()
    }

    private val RecognitionStatus.isReadyToRecognize get() = when (this) {
        RecognitionStatus.Ready,
        is RecognitionStatus.Done -> true
        is RecognitionStatus.Recognizing -> false
    }

    companion object {
        fun requestListeningState(context: Context) {
            try {
                requestListeningState(
                    context.applicationContext,
                    ComponentName(context.applicationContext, OneTimeRecognitionTileService::class.java)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request qsTile listening state", e)
            }
        }
    }
}
