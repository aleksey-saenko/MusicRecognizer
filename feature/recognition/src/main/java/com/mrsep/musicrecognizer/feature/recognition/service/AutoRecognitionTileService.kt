package com.mrsep.musicrecognizer.feature.recognition.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val TAG = "AutoRecognitionTileService"

@AndroidEntryPoint
class AutoRecognitionTileService : TileService() {

    private var coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tileUpdateJob: Job? = null

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

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
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        tileUpdateJob?.cancel()
        tileUpdateJob = null
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        if (AutoRecognitionService.isRunning) {
            AutoRecognitionService.stop(applicationContext)
            coroutineScope.launch {
                preferencesRepository.setAutoRecognizeEnabled(false)
            }
            tileUpdateJob = coroutineScope.launch {
                kotlinx.coroutines.delay(500)
                updateTile()
            }
        } else {
            // Must launch through an activity to get foreground state for microphone FGS
            coroutineScope.launch {
                preferencesRepository.setAutoRecognizeEnabled(true)
            }
            if (Build.VERSION.SDK_INT >= 34) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        AutoRecognitionLauncherActivity.startIntent(applicationContext),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(
                    AutoRecognitionLauncherActivity.startIntent(applicationContext)
                )
            }
            tileUpdateJob = coroutineScope.launch {
                kotlinx.coroutines.delay(1500)
                updateTile()
            }
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: run {
            Log.e(TAG, "qsTile is null, is update called in valid state?")
            return
        }

        tile.label = getString(
            if (AutoRecognitionService.isRunning) {
                StringsR.string.auto_recognize_tile_active
            } else {
                StringsR.string.auto_recognize_tile_inactive
            }
        )
        tile.state = if (AutoRecognitionService.isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.icon = Icon.createWithResource(this, UiR.drawable.ic_auto_recognize)
        tile.updateTile()
    }

    companion object {
        fun requestListeningState(context: Context) {
            try {
                requestListeningState(
                    context.applicationContext,
                    ComponentName(context.applicationContext, AutoRecognitionTileService::class.java)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request qsTile listening state", e)
            }
        }
    }
}
