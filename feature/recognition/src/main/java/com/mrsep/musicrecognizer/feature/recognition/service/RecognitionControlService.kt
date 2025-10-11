package com.mrsep.musicrecognizer.feature.recognition.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioCaptureConfig
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioRecordingControllerFactory
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionInteractor
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.di.MainScreenStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.MutableRecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.service.ServiceNotificationHelper.Companion.NOTIFICATION_ID_STATUS
import com.mrsep.musicrecognizer.feature.recognition.service.ext.downloadImageToDiskCache
import com.mrsep.musicrecognizer.feature.recognition.widget.RecognitionWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlinx.parcelize.Parcelize

private const val TAG = "RecognitionControlService"

@AndroidEntryPoint
@OptIn(ExperimentalCoroutinesApi::class)
class RecognitionControlService : Service() {

    @Inject
    lateinit var recognitionInteractor: RecognitionInteractor

    @Inject
    lateinit var audioRecordingControllerFactory: AudioRecordingControllerFactory

    @Inject
    @MainScreenStatusHolder
    internal lateinit var screenStatusHolder: MutableRecognitionStatusHolder

    @Inject
    @WidgetStatusHolder
    internal lateinit var widgetStatusHolder: MutableRecognitionStatusHolder

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    internal lateinit var resultNotificationHelper: ResultNotificationHelper

    @Inject
    internal lateinit var serviceNotificationHelper: ServiceNotificationHelper

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    private val appContext get() = applicationContext

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val actionReceiver = ActionBroadcastReceiver()

    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var recognitionJob: Job? = null
    private val isRecognitionJobCompleted get() = recognitionJob?.isCompleted ?: true

    private var cancelRecognitionJob: Job? = null
    private val isCancelRecognitionJobCompleted get() = cancelRecognitionJob?.isCompleted ?: true

    private var isStartedForeground = false

    // Hold mode keeps the service in running state with an ongoing ready notification, waiting for new requests
    private var isHoldModeActive = false

    private var mediaProjection: MediaProjection? = null
    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() = onCancelRecognition()
    }

    private val soundLevelCurrentFlow = MutableStateFlow(flowOf(0f))
    private val soundLevelState = soundLevelCurrentFlow
        .transformLatest { emitAll(it) }
        .stateIn(
            scope = serviceScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = 0f
        )

    override fun onCreate() {
        super.onCreate() // Required by hilt injection
        registerActionReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            // Any recognition launch must be performed either from a visible activity or widget,
            // or from a notification or another pending action that allows microphone access for the service
            ACTION_LAUNCH_RECOGNITION -> run {
                if (!isRecognitionJobCompleted || !isCancelRecognitionJobCompleted) return@run
                val audioCaptureServiceMode = IntentCompat.getParcelableExtra(
                    intent,
                    KEY_AUDIO_CAPTURE_SERVICE_MODE,
                    AudioCaptureServiceMode::class.java
                )
                requireNotNull(audioCaptureServiceMode) { "Audio capture mode is not specified" }
                when (audioCaptureServiceMode) {
                    AudioCaptureServiceMode.Microphone -> {
                        startForegroundWithType(false)
                    }

                    is AudioCaptureServiceMode.Device -> {
                        val mediaProjection = audioCaptureServiceMode.mediaProjectionData != null
                        check(!mediaProjection || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            "AudioPlaybackCapture API is available on Android 10+"
                        }
                        startForegroundWithType(mediaProjection)
                    }
                    is AudioCaptureServiceMode.Auto -> {
                        val mediaProjection = audioCaptureServiceMode.mediaProjectionData != null
                        check(!mediaProjection || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            "AudioPlaybackCapture API is available on Android 10+"
                        }
                        startForegroundWithType(mediaProjection)
                    }
                }
                if (!isStartedForeground) return@run
                onLaunchRecognition(audioCaptureServiceMode)
            }

            // Start with null intent is considered as restart of sticky service with hold mode on
            null,
            ACTION_HOLD_MODE_ON -> run {
                isHoldModeActive = true
                if (!isRecognitionJobCompleted || !isCancelRecognitionJobCompleted) return@run
                if (!isStartedForeground) startForegroundWithType(false)
            }

            else -> error("Unknown service start intent")
        }
        return if (isHoldModeActive) START_STICKY else START_NOT_STICKY
    }

    override fun onBind(intent: Intent): Binder {
        return when (intent.action) {
            ACTION_BIND_MAIN_SCREEN -> object : MainScreenBinder() {
                override val soundLevel = soundLevelState
            }

            else -> error("Unknown service bind intent")
        }
    }

    override fun onDestroy() {
        unregisterReceiver(actionReceiver)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun registerActionReceiver() {
        ContextCompat.registerReceiver(
            this,
            actionReceiver,
            IntentFilter().apply {
                addAction(LOCAL_ACTION_CANCEL_RECOGNITION)
                addAction(LOCAL_ACTION_STOP_HOLD_MODE)
                addAction(LOCAL_ACTION_DISABLE_SERVICE)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    // Can be called even if the service is already in foreground state to change service type.
    // We must drop mediaProjection type after each recognition to allow sticky FGS restart.
    private fun startForegroundWithType(mediaProjection: Boolean) {
        val initialNotification = getInitialForegroundNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID_STATUS,
                    initialNotification,
                    if (mediaProjection) {
                        FOREGROUND_SERVICE_TYPE_MICROPHONE.or(FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                    } else {
                        FOREGROUND_SERVICE_TYPE_MICROPHONE
                    }
                )
            } else {
                startForeground(NOTIFICATION_ID_STATUS, initialNotification)
            }
            isStartedForeground = true
        } catch (e: SecurityException) {
            val msg = "Foreground service cannot start due to denied permissions"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            Log.w(TAG, msg, e)
            serviceScope.launch {
                preferencesRepository.setNotificationServiceEnabled(false)
                stopSelf()
            }
            return
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) throw e
            if (e is ForegroundServiceStartNotAllowedException) {
                val msg = "Foreground service cannot start from the background"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                Log.e(TAG, msg, e)
                stopSelf()
                return
            }
        }
    }

    private fun getInitialForegroundNotification(): Notification {
        return when (val status = recognitionInteractor.status.value) {
            RecognitionStatus.Ready,
            is RecognitionStatus.Done -> {
                serviceNotificationHelper.buildReadyNotification()
            }
            is RecognitionStatus.Recognizing -> {
                serviceNotificationHelper.buildListeningNotification(status.extraTime)
            }
        }
    }

    private fun onLaunchRecognition(audioCaptureServiceMode: AudioCaptureServiceMode) {
        if (!isRecognitionJobCompleted || !isCancelRecognitionJobCompleted) return
        cancelRecognitionJob = null
        recognitionJob = serviceScope.launch {
            recognitionInteractor.cancelAndJoin()
            val preferences = preferencesRepository.userPreferencesFlow.first()
            // Recognition can be launched from detached notification
            isHoldModeActive = preferences.notificationServiceEnabled
            val captureConfig = when (audioCaptureServiceMode) {
                AudioCaptureServiceMode.Microphone -> AudioCaptureConfig.Microphone
                is AudioCaptureServiceMode.Device -> if (audioCaptureServiceMode.mediaProjectionData != null) {
                    mediaProjectionManager.getMediaProjection(
                        Activity.RESULT_OK,
                        audioCaptureServiceMode.mediaProjectionData
                    )?.run {
                        mediaProjection = this
                        registerCallback(mediaProjectionCallback, Handler(appContext.mainLooper))
                        AudioCaptureConfig.Device(this)
                    }
                } else {
                    AudioCaptureConfig.Device(null)
                }

                is AudioCaptureServiceMode.Auto ->if (audioCaptureServiceMode.mediaProjectionData != null) {
                    mediaProjectionManager.getMediaProjection(
                        Activity.RESULT_OK,
                        audioCaptureServiceMode.mediaProjectionData
                    )?.run {
                        mediaProjection = this
                        registerCallback(mediaProjectionCallback, Handler(appContext.mainLooper))
                        AudioCaptureConfig.Auto(this)
                    }
                } else {
                    AudioCaptureConfig.Auto(null)
                }
            }
            if (captureConfig == null) return@launch
            val recorderController = audioRecordingControllerFactory.getAudioController(captureConfig)

            soundLevelCurrentFlow.update { recorderController.soundLevel }

            recognitionInteractor.launchRecognition(recorderController)
            recognitionInteractor.status.transformWhile { status ->
                emit(status)
                status !is RecognitionStatus.Done
            }.collect { status ->
                when (status) {
                    RecognitionStatus.Ready -> {} // NO-OP

                    is RecognitionStatus.Recognizing -> {
                        // Cancel last result notification if it exists
                        resultNotificationHelper.cancelResultNotification()
                        screenStatusHolder.updateStatus(status)
                        widgetStatusHolder.updateStatus(status)
                        requestWidgetsUpdate()
                        requestQuickTileUpdate()
                        serviceNotificationHelper.notifyListeningStatus(status.extraTime)
                    }

                    is RecognitionStatus.Done -> {
                        val recognitionResult = status.result
                        if (recognitionResult is RecognitionResult.Success) {
                            listOfNotNull(
                                recognitionResult.track.artworkThumbUrl,
                                recognitionResult.track.artworkUrl
                            ).map { imageUrl ->
                                async { downloadImageToDiskCache(imageUrl) }
                            }.awaitAll()
                        }
                        val isScreenUpdated = screenStatusHolder.updateStatusIfObserving(status)
                        if (isScreenUpdated) {
                            widgetStatusHolder.updateStatus(RecognitionStatus.Ready)
                            resultNotificationHelper.notifyForegroundResult(status.result)
                        } else {
                            screenStatusHolder.updateStatus(RecognitionStatus.Ready)
                            if (hasActiveWidgets()) {
                                widgetStatusHolder.updateStatus(status)
                            } else {
                                widgetStatusHolder.updateStatus(RecognitionStatus.Ready)
                            }
                            resultNotificationHelper.notifyBackgroundResult(status.result)
                        }
                        requestWidgetsUpdate()
                        requestQuickTileUpdate()
                        recognitionInteractor.resetFinalStatus()
                    }
                }
            }
            stopMediaProjection()
            soundLevelCurrentFlow.update { flowOf(0f) }

            if (isHoldModeActive) {
                startForegroundWithType(false)
            } else {
                stopSelf()
            }
        }
    }

    private fun onCancelRecognition() {
        if (isRecognitionJobCompleted) return
        if (!isCancelRecognitionJobCompleted) return
        cancelRecognitionJob = serviceScope.launch {
            soundLevelCurrentFlow.update { flowOf(0f) }
            recognitionJob?.cancelAndJoin()
            recognitionJob = null
            recognitionInteractor.cancelAndJoin()
            stopMediaProjection()
            screenStatusHolder.updateStatus(RecognitionStatus.Ready)
            widgetStatusHolder.updateStatus(RecognitionStatus.Ready)
            requestWidgetsUpdate()
            requestQuickTileUpdate()

            if (isHoldModeActive) {
                startForegroundWithType(false)
            } else {
                stopSelf()
            }
        }
    }

    private fun stopMediaProjection() {
        mediaProjection?.unregisterCallback(mediaProjectionCallback)
        mediaProjection?.stop()
        mediaProjection = null
    }

    private suspend fun hasActiveWidgets(): Boolean {
        return GlanceAppWidgetManager(appContext)
            .getGlanceIds(RecognitionWidget::class.java)
            .isNotEmpty()
    }

    private suspend fun requestWidgetsUpdate() {
        RecognitionWidget().updateAll(appContext)
    }

    private fun requestQuickTileUpdate() {
        OneTimeRecognitionTileService.requestListeningState(appContext)
    }

    private inner class ActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {

                LOCAL_ACTION_CANCEL_RECOGNITION -> onCancelRecognition()

                LOCAL_ACTION_STOP_HOLD_MODE -> {
                    isHoldModeActive = false
                    if (!isRecognitionJobCompleted || !isCancelRecognitionJobCompleted) return
                    stopSelf()
                }

                LOCAL_ACTION_DISABLE_SERVICE -> {
                    isHoldModeActive = false
                    if (!isRecognitionJobCompleted) onCancelRecognition()
                    appScope.launch {
                        cancelRecognitionJob?.join()
                        stopSelf()
                    }
                }

                else -> error("Unknown intent action")
            }
        }
    }

    abstract class MainScreenBinder : Binder() {
        // TODO Consider to use binders to distribute recognition status
//        abstract val status: StateFlow<RecognitionStatus>
        abstract val soundLevel: StateFlow<Float>
    }

    companion object {
        // Actions that require service to start if it is not yet
        private const val ACTION_LAUNCH_RECOGNITION = "com.mrsep.musicrecognizer.service.action.launch_recognition"
        private const val ACTION_HOLD_MODE_ON = "com.mrsep.musicrecognizer.service.action.hold_mode_on"

        private const val ACTION_BIND_MAIN_SCREEN = "com.mrsep.musicrecognizer.service.action.bind_main_screen"

        // Local broadcast actions to control running service
        private const val LOCAL_ACTION_CANCEL_RECOGNITION = "com.mrsep.musicrecognizer.service.action.local.cancel_recognition"
        private const val LOCAL_ACTION_STOP_HOLD_MODE = "com.mrsep.musicrecognizer.service.action.local.stop_hold_mode"
        private const val LOCAL_ACTION_DISABLE_SERVICE = "com.mrsep.musicrecognizer.service.action.local.disable_service"

        private const val KEY_AUDIO_CAPTURE_SERVICE_MODE = "KEY_AUDIO_CAPTURE_SERVICE_MODE"

        fun startRecognition(context: Context, audioCaptureServiceMode: AudioCaptureServiceMode) {
            context.startForegroundService(
                Intent(context, RecognitionControlService::class.java)
                    .setAction(ACTION_LAUNCH_RECOGNITION)
                    .putExtra(KEY_AUDIO_CAPTURE_SERVICE_MODE, audioCaptureServiceMode)
            )
        }

        fun cancelRecognition(context: Context) {
            context.sendBroadcast(cancelRecognitionBroadcastIntent(context))
        }

        fun cancelRecognitionPendingIntent(context: Context): PendingIntent {
            return PendingIntent.getBroadcast(
                context,
                0,
                cancelRecognitionBroadcastIntent(context),
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun cancelRecognitionBroadcastIntent(context: Context): Intent {
            return Intent(LOCAL_ACTION_CANCEL_RECOGNITION)
                .setPackage(context.packageName)
        }

        fun bindMainScreenIntent(context: Context): Intent {
            return Intent(context, RecognitionControlService::class.java)
                .setAction(ACTION_BIND_MAIN_SCREEN)
        }

        fun startHoldMode(context: Context, fromBackground: Boolean) {
            if (fromBackground) {
                ServiceNotificationHelper(context).notifyDetachedReadyStatusIfServiceStopped()
            } else {
                context.startForegroundService(
                    Intent(context, RecognitionControlService::class.java)
                        .setAction(ACTION_HOLD_MODE_ON)
                )
            }
        }

        fun stopHoldMode(context: Context) {
            ServiceNotificationHelper(context).cancelDetachedReadyStatus()
            context.sendBroadcast(
                Intent(LOCAL_ACTION_STOP_HOLD_MODE)
                    .setPackage(context.packageName)
            )
        }

        fun stopServiceGracefully(context: Context) {
            context.sendBroadcast(
                Intent(LOCAL_ACTION_DISABLE_SERVICE)
                    .setPackage(context.packageName)
            )
        }
    }
}

@Parcelize
sealed class AudioCaptureServiceMode : Parcelable {
    data object Microphone : AudioCaptureServiceMode()
    data class Device(val mediaProjectionData: Intent?) : AudioCaptureServiceMode()
    data class Auto(val mediaProjectionData: Intent?) : AudioCaptureServiceMode()
}
