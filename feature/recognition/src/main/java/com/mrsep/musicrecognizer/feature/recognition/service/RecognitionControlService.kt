package com.mrsep.musicrecognizer.feature.recognition.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
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
import com.mrsep.musicrecognizer.core.domain.recognition.NetworkMonitor
import com.mrsep.musicrecognizer.feature.recognition.di.MainScreenStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.MutableRecognitionStatusHolder
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
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR
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
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    internal lateinit var notificationHelper: ResultNotificationHelper

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val actionReceiver by lazy { ActionBroadcastReceiver() }
    private var isActionReceiverRegistered = false

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var recognitionJob: Job? = null
    private val isRecognitionJobActive get() = recognitionJob?.isActive == true
    private var recognitionCancelingJob: Job? = null
    private val isRecognitionCancelingJobActive get() = recognitionCancelingJob?.isActive == true

    private var isStartedForeground = false

    /* Hold mode means keeping an ongoing control notification for starting new recognition,
     in this mode the service runs in the foreground with type FOREGROUND_SERVICE_TYPE_SPECIAL_USE */
    private var isHoldModeActive = false

    private var mediaProjection: MediaProjection? = null
    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() = cancelRecognitionJob()
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
        /* Check again because permissions can be restricted in any time,
         which can lead sticky service to restart foreground with SecurityException */
        val startAllowed = checkRecognitionControlServicePermissions()
        if (!startAllowed) {
            Log.e(TAG, "Service start is not allowed due to denied permissions")
            appScope.launch {
                preferencesRepository.setNotificationServiceEnabled(false)
            }
            stopSelf()
            return
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            /* Any recognition launch must be performed either from a visible activity or widget,
             or from a notification or another pending action that allows microphone access for the service */
            ACTION_LAUNCH_RECOGNITION -> run {
                if (isRecognitionJobActive) return@run
                val audioCaptureServiceMode = IntentCompat.getParcelableExtra(
                    intent,
                    KEY_AUDIO_CAPTURE_SERVICE_MODE,
                    AudioCaptureServiceMode::class.java
                )
                requireNotNull(audioCaptureServiceMode) {
                    "Starting service without specifying audio capture mode is not allowed"
                }
                when (audioCaptureServiceMode) {
                    AudioCaptureServiceMode.Microphone -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            continueInForeground(serviceTypesForRecognition(false))
                        } else {
                            continueInForeground()
                        }
                    }

                    is AudioCaptureServiceMode.Device,
                    is AudioCaptureServiceMode.Auto
                        -> {
                        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            "AudioPlaybackCapture API is available on Android 10+"
                        }
                        // Starting media projection is only allowed while the service is running in foreground
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            continueInForeground(serviceTypesForRecognition(true))
                        } else {
                            continueInForeground()
                        }
                    }
                }
                launchRecognition(audioCaptureServiceMode)
            }

            ACTION_CANCEL_RECOGNITION -> {
                if (isRecognitionJobActive) {
                    cancelRecognitionJob()
                } else if (!isHoldModeActive) {
                    stopSelf()
                }
            }

            // Start with null intent is considered as restart of sticky service with hold mode on
            null,
            ACTION_HOLD_MODE_ON -> run {
                isHoldModeActive = true
                if (isRecognitionJobActive || isRecognitionCancelingJobActive) return@run
                if (!isStartedForeground) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        continueInForeground(FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                    } else {
                        continueInForeground()
                    }
                }
            }

            ACTION_HOLD_MODE_OFF -> run {
                isHoldModeActive = false
                if (isRecognitionJobActive || isRecognitionCancelingJobActive) return@run
                stopSelf()
            }

            else -> error("Unknown service start intent")
        }
        return if (isHoldModeActive) START_STICKY else START_NOT_STICKY
    }

    override fun onBind(intent: Intent): Binder {
        return when (intent.action) {
            ACTION_BIND_MAIN_SCREEN -> {
                object : MainScreenBinder() {
                    override val soundLevel = soundLevelState
                }
            }

            else -> error("Unknown service bind intent")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterActionReceiver()
        serviceScope.cancel()
    }

    private fun getInitialForegroundNotification(): Notification {
        return if (isRecognitionJobActive) {
            statusNotificationBuilder()
                .setContentTitle(getString(StringsR.string.notification_listening))
                .setContentText(getString(StringsR.string.notification_listening_subtitle))
                .setSmallIcon(UiR.drawable.ic_notification_listening)
                .addCancelButton()
                .build()
        } else {
            statusNotificationBuilder()
                .setContentTitle(getString(StringsR.string.notification_tap_to_recognize))
                .setContentText(getString(StringsR.string.notification_tap_to_recognize_subtitle))
                .addRecognizeContentIntent()
                .addOptionalDisableButton()
                .build()
        }
    }

    private fun continueInForeground() {
        val initialNotification = getInitialForegroundNotification()
        startForeground(NOTIFICATION_ID_STATUS, initialNotification)
        isStartedForeground = true
        onStartedInForeground()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun serviceTypesForRecognition(mediaProjection: Boolean) = if (mediaProjection) {
        FOREGROUND_SERVICE_TYPE_MICROPHONE.or(FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    } else {
        FOREGROUND_SERVICE_TYPE_MICROPHONE
    }

    // Can be called even if the service is already in foreground state to change service type
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun continueInForeground(foregroundServiceType: Int) {
        val initialNotification = getInitialForegroundNotification()
        startForeground(
            NOTIFICATION_ID_STATUS,
            initialNotification,
            foregroundServiceType
        )
        isStartedForeground = true
        onStartedInForeground()
    }

    private fun onStartedInForeground() {
        if (!isActionReceiverRegistered) registerActionReceiver()
    }

    private fun registerActionReceiver() {
        ContextCompat.registerReceiver(
            this,
            actionReceiver,
            IntentFilter().apply {
                addAction(LOCAL_ACTION_CANCEL_RECOGNITION)
                addAction(LOCAL_ACTION_DISABLE_SERVICE)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isActionReceiverRegistered = true
    }

    private fun unregisterActionReceiver() {
        if (!isActionReceiverRegistered) return
        unregisterReceiver(actionReceiver)
        isActionReceiverRegistered = false
    }

    private fun statusNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_STATUS)
            .setSmallIcon(UiR.drawable.ic_notification_ready)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(true) // Can be dismissed since API 34, keep in mind
            .setCategory(Notification.CATEGORY_STATUS)
            .setSilent(true) // Avoids alert sound during recording
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addDismissIntent()
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun NotificationCompat.Builder.addRecognizeContentIntent(): NotificationCompat.Builder {
        val intent = Intent(
            this@RecognitionControlService,
            RecognitionControlActivity::class.java
        ).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            action = ACTION_LAUNCH_RECOGNITION
        }
        val pendingIntent = PendingIntent.getActivity(
            this@RecognitionControlService,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return setContentIntent(pendingIntent)
    }

    /* Status notification is ongoing for API 33 and below.
     Starting from API 34, users can disable the service by dismissing control notification */
    private fun NotificationCompat.Builder.addOptionalDisableButton(): NotificationCompat.Builder {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(StringsR.string.notification_button_disable_service),
                PendingIntent.getBroadcast(
                    this@RecognitionControlService,
                    0,
                    Intent(LOCAL_ACTION_DISABLE_SERVICE).setPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        } else {
            this
        }
    }

    private fun NotificationCompat.Builder.addCancelButton(): NotificationCompat.Builder {
        return addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            getString(StringsR.string.cancel),
            PendingIntent.getBroadcast(
                this@RecognitionControlService,
                0,
                Intent(LOCAL_ACTION_CANCEL_RECOGNITION).setPackage(packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun NotificationCompat.Builder.addDismissIntent(): NotificationCompat.Builder {
        return setDeleteIntent(
            PendingIntent.getBroadcast(
                this@RecognitionControlService,
                0,
                Intent(LOCAL_ACTION_DISABLE_SERVICE).setPackage(packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun NotificationCompat.Builder.buildAndNotifyAsStatus() {
        notificationManager.notify(NOTIFICATION_ID_STATUS, this.build())
    }

    private fun launchRecognition(audioCaptureServiceMode: AudioCaptureServiceMode) {
        if (isRecognitionJobActive) return
        if (isRecognitionCancelingJobActive) return
        if (recognitionInteractor.status.value != RecognitionStatus.Ready) return

        recognitionJob = serviceScope.launch {

            val captureConfig = when (audioCaptureServiceMode) {
                AudioCaptureServiceMode.Microphone -> AudioCaptureConfig.Microphone
                is AudioCaptureServiceMode.Device -> {
                    mediaProjectionManager.getMediaProjection(
                        Activity.RESULT_OK,
                        audioCaptureServiceMode.mediaProjectionData
                    )?.run {
                        mediaProjection = this
                        registerCallback(mediaProjectionCallback, null)
                        AudioCaptureConfig.Device(this)
                    }
                }

                is AudioCaptureServiceMode.Auto -> {
                    mediaProjectionManager.getMediaProjection(
                        Activity.RESULT_OK,
                        audioCaptureServiceMode.mediaProjectionData
                    )?.run {
                        mediaProjection = this
                        registerCallback(mediaProjectionCallback, null)
                        AudioCaptureConfig.Auto(this)
                    }
                }
            }
            if (captureConfig == null) return@launch
            val recorderController = audioRecordingControllerFactory.getAudioController(captureConfig)

            soundLevelCurrentFlow.update { recorderController.soundLevel }

            if (networkMonitor.isOffline.first()) {
                recognitionInteractor.launchOfflineRecognition(serviceScope, recorderController)
            } else {
                recognitionInteractor.launchRecognition(serviceScope, recorderController)
            }
            recognitionInteractor.status.transformWhile { status ->
                emit(status)
                status !is RecognitionStatus.Done
            }.collect { status ->
                when (status) {
                    RecognitionStatus.Ready -> {} // NO-OP

                    is RecognitionStatus.Recognizing -> {
                        // Cancel last result notification if it exists
                        notificationHelper.cancelResultNotification()
                        screenStatusHolder.updateStatus(status)
                        widgetStatusHolder.updateStatus(status)
                        requestWidgetsUpdate()
                        requestQuickTileUpdate()
                        notifyRecognizing(status.extraTry)
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
                        } else {
                            notificationHelper.notifyResult(status.result)
                            screenStatusHolder.updateStatus(RecognitionStatus.Ready)
                            if (hasActiveWidgets()) {
                                widgetStatusHolder.updateStatus(status)
                            } else {
                                widgetStatusHolder.updateStatus(RecognitionStatus.Ready)
                            }
                        }
                        requestWidgetsUpdate()
                        requestQuickTileUpdate()
                        recognitionInteractor.resetFinalStatus()
                    }
                }
            }
            manualStopMediaProjection()
            soundLevelCurrentFlow.update { flowOf(0f) }
        }.apply {
            invokeOnCompletion { cause ->
                if (cause != null) return@invokeOnCompletion
                if (isHoldModeActive) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        continueInForeground(FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                    } else {
                        continueInForeground()
                    }
                } else {
                    stopSelf()
                }
            }
        }
    }

    private fun manualStopMediaProjection() {
        mediaProjection?.unregisterCallback(mediaProjectionCallback)
        mediaProjection?.stop()
        mediaProjection = null
    }

    private fun cancelRecognitionJob() {
        if (isRecognitionCancelingJobActive) return
        recognitionCancelingJob = serviceScope.launch {
            soundLevelCurrentFlow.update { flowOf(0f) }
            recognitionJob?.cancelAndJoin()
            recognitionInteractor.cancelAndJoin()
            manualStopMediaProjection()
            screenStatusHolder.updateStatus(RecognitionStatus.Ready)
            widgetStatusHolder.updateStatus(RecognitionStatus.Ready)
            requestWidgetsUpdate()
            requestQuickTileUpdate()
            if (isHoldModeActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    continueInForeground(FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } else {
                    continueInForeground()
                }
            } else {
                stopSelf()
            }
        }
    }

    private fun notifyRecognizing(extraTry: Boolean) {
        statusNotificationBuilder()
            .setContentTitle(getString(StringsR.string.notification_listening))
            .setContentText(
                if (extraTry) {
                    getString(StringsR.string.notification_listening_subtitle_extra_time)
                } else {
                    getString(StringsR.string.notification_listening_subtitle)
                }
            )
            .setSmallIcon(UiR.drawable.ic_notification_listening)
            .addCancelButton()
            .buildAndNotifyAsStatus()
    }

    private suspend fun hasActiveWidgets(): Boolean {
        return GlanceAppWidgetManager(this)
            .getGlanceIds(RecognitionWidget::class.java)
            .isNotEmpty()
    }

    private suspend fun requestWidgetsUpdate() {
        RecognitionWidget().updateAll(this)
    }

    private fun requestQuickTileUpdate() {
        OneTimeRecognitionTileService.requestListeningState(this)
    }

    private fun checkRecognitionControlServicePermissions(): Boolean {
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

    private inner class ActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                LOCAL_ACTION_CANCEL_RECOGNITION -> {
                    cancelRecognitionJob()
                }

                LOCAL_ACTION_DISABLE_SERVICE -> {
                    serviceScope.launch {
                        if (isHoldModeActive) {
                            preferencesRepository.setNotificationServiceEnabled(false)
                            isHoldModeActive = false
                        }
                        if (isRecognitionJobActive) {
                            cancelRecognitionJob()
                        } else {
                            stopSelf()
                        }
                    }
                }
            }
        }
    }

    abstract class MainScreenBinder : Binder() {
        // TODO Consider to use binders to distribute recognition status
//        abstract val status: StateFlow<RecognitionStatus>
        abstract val soundLevel: StateFlow<Float>
    }

    companion object {
        const val KEY_AUDIO_CAPTURE_SERVICE_MODE = "KEY_AUDIO_CAPTURE_SERVICE_MODE"

        const val ACTION_LAUNCH_RECOGNITION = "com.mrsep.musicrecognizer.service.action.launch_recognition"
        const val ACTION_CANCEL_RECOGNITION = "com.mrsep.musicrecognizer.service.action.cancel_recognition"
        const val ACTION_HOLD_MODE_ON = "com.mrsep.musicrecognizer.service.action.hold_mode_on"
        const val ACTION_HOLD_MODE_OFF = "com.mrsep.musicrecognizer.service.action.hold_mode_off"

        const val ACTION_BIND_MAIN_SCREEN = "com.mrsep.musicrecognizer.service.action.bind_main_screen"

        // Actions for local broadcasting
        private const val LOCAL_ACTION_CANCEL_RECOGNITION = "com.mrsep.musicrecognizer.service.action.local.cancel_recognition"
        private const val LOCAL_ACTION_DISABLE_SERVICE = "com.mrsep.musicrecognizer.service.action.local.disable_service"

        private const val NOTIFICATION_ID_STATUS = 1
        private const val NOTIFICATION_CHANNEL_ID_STATUS = "com.mrsep.musicrecognizer.status"

        fun getChannelForRecognitionStatuses(context: Context): NotificationChannel {
            val name = context.getString(StringsR.string.notification_channel_name_control)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            return NotificationChannel(NOTIFICATION_CHANNEL_ID_STATUS, name, importance).apply {
               description = context.getString(StringsR.string.notification_channel_desc_control)
               lockscreenVisibility = Notification.VISIBILITY_PUBLIC
               setShowBadge(false)
               enableLights(false)
               enableVibration(false)
           }
        }
    }
}

@Parcelize
internal sealed class AudioCaptureServiceMode : Parcelable {
    data object Microphone : AudioCaptureServiceMode()
    data class Device(val mediaProjectionData: Intent) : AudioCaptureServiceMode()
    data class Auto(val mediaProjectionData: Intent) : AudioCaptureServiceMode()
}
