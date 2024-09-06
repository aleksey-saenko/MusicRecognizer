package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.mrsep.musicrecognizer.core.common.DispatchersProvider
import com.mrsep.musicrecognizer.core.ui.util.dpToPx
import com.mrsep.musicrecognizer.feature.recognition.di.MainScreenStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.NetworkMonitor
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.MutableRecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.ext.artistWithAlbumFormatted
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.ext.downloadImageToDiskCache
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.ext.getCachedImageOrNull
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.ext.getSharedBody
import com.mrsep.musicrecognizer.feature.recognition.widget.RecognitionWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformWhile
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@AndroidEntryPoint
class NotificationService : Service() {

    @Inject
    lateinit var recognitionInteractor: RecognitionInteractor

    @Inject
    @MainScreenStatusHolder
    internal lateinit var screenStatusHolder: MutableRecognitionStatusHolder

    @Inject
    @WidgetStatusHolder
    internal lateinit var widgetStatusHolder: MutableRecognitionStatusHolder

    @Inject
    lateinit var serviceRouter: NotificationServiceRouter

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var dispatchersProvider: DispatchersProvider

    private val serviceScope by lazy {
        CoroutineScope(dispatchersProvider.main + SupervisorJob())
    }

    private val actionReceiver by lazy { ActionBroadcastReceiver() }
    private var isActionReceiverRegistered = false

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private var recognitionJob: Job? = null
    private val isRecognitionJobActive get() = recognitionJob?.isActive == true
    private var recognitionCancelingJob: Job? = null
    private val isRecognitionCancelingJobActive get() = recognitionCancelingJob?.isActive == true

    // Service started from background (e.g., on device boot) doesn't have microphone access
    private var isMicrophoneRestricted = false

    private var isStartedForeground = false

    // If true, the service must stay started foreground after recognition completion,
    // even if recognition screen is resumed
    private var isHoldModeActive = false

    override fun onBind(intent: Intent) = null

    override fun onCreate() {
        super.onCreate() // required by hilt injection
        // Check again because permissions can be restricted in any time,
        // which can lead sticky service to restart foreground with SecurityException
        val startAllowed = checkNotificationServicePermissions()
        if (!startAllowed) {
            Log.e(
                "NotificationService",
                "Service start is not allowed due to restricted permissions"
            )
            runBlocking {
                preferencesRepository.setNotificationServiceEnabled(false)
                stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            LAUNCH_RECOGNITION_ACTION -> {
                val foregroundRequested = intent.getBooleanExtra(
                    KEY_FOREGROUND_REQUESTED,
                    true
                )
                // Consider that all launch recognition requests are performed from widgets,
                // notifications or another pending actions
                // that grant microphone access for the service
                if (foregroundRequested && (!isStartedForeground || isMicrophoneRestricted)) {
                    isMicrophoneRestricted = false
                    // This also restarts foreground with microphone type
                    continueInForeground()
                }
                isMicrophoneRestricted = false
                launchRecognition()
            }

            CANCEL_RECOGNITION_ACTION -> {
                if (isRecognitionJobActive) {
                    cancelRecognitionJob()
                } else if (!isHoldModeActive) {
                    stopSelf()
                }
            }

            // Start with null intent is considered as restart of sticky service with hold mode on
            // isMicrophoneRestricted is true when service is started on device boot without microphone access
            null,
            HOLD_MODE_ON_ACTION -> {
                isMicrophoneRestricted = intent?.getBooleanExtra(
                    KEY_RESTRICTED_START,
                    true
                ) ?: true
                isHoldModeActive = true
                if (!isStartedForeground) continueInForeground()
            }

            HOLD_MODE_OFF_ACTION -> {
                isHoldModeActive = false
                if (!isRecognitionJobActive) {
                    stopSelf()
                }
            }

            else -> {
                throw IllegalStateException("NotificationService: Unknown start intent")
            }
        }
        return if (isHoldModeActive) START_STICKY else START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterActionReceiver()
        serviceScope.cancel()
    }

    // Can be called even if the service is already in foreground state
    // with FOREGROUND_SERVICE_TYPE_SPECIAL_USE to restart with FOREGROUND_SERVICE_TYPE_MICROPHONE
    private fun continueInForeground() {
        val initialNotification = if (isRecognitionJobActive) {
            statusNotificationBuilder()
                .setContentTitle(getString(StringsR.string.listening))
                .setContentText(getString(StringsR.string.please_wait))
                .addCancelButton()
                .build()
        } else {
            statusNotificationBuilder()
                .setContentTitle(getString(StringsR.string.tap_to_recognize_short))
                .setContentText(getString(StringsR.string.identify_while_using_another_app_short))
                .addRecognizeContentIntent()
                .addOptionalDisableButton()
                .build()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                STATUS_NOTIFICATION_ID,
                initialNotification,
                if (isMicrophoneRestricted) {
                    FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    FOREGROUND_SERVICE_TYPE_MICROPHONE
                }
            )
        } else {
            startForeground(STATUS_NOTIFICATION_ID, initialNotification)
        }
        isStartedForeground = true
        if (!isActionReceiverRegistered) registerActionReceiver()
    }

    private fun continueInBackground() {
        unregisterActionReceiver()
        stopForeground(STOP_FOREGROUND_REMOVE)
        isStartedForeground = false
    }

    private fun registerActionReceiver() {
        ContextCompat.registerReceiver(
            this,
            actionReceiver,
            IntentFilter().apply {
                addAction(CANCEL_RECOGNITION_LOCAL_ACTION)
                addAction(DISABLE_SERVICE_LOCAL_ACTION)
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

    private fun createPendingIntent(intent: Intent): PendingIntent {
        return TaskStackBuilder.create(this@NotificationService).run {
            addNextIntentWithParentStack(intent)
            // FLAG_UPDATE_CURRENT to update trackId key on each result
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private fun statusNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_STATUS_CHANNEL_ID)
            .setSmallIcon(UiR.drawable.ic_retro_microphone)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(true) // can be dismissed since API 34
            .setCategory(Notification.CATEGORY_STATUS)
            .setSilent(true) // to avoid alert sound in recording
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addDismissIntent()
    }

    private fun resultNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_RESULT_CHANNEL_ID)
            .setSmallIcon(UiR.drawable.ic_retro_microphone)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setOngoing(false)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun NotificationCompat.Builder.addRecognizeContentIntent(): NotificationCompat.Builder {
        val intent = Intent(
            this@NotificationService,
            NotificationService::class.java
        ).apply {
            action = LAUNCH_RECOGNITION_ACTION
        }
        val pendingIntent = PendingIntent.getService(
            this@NotificationService,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return setContentIntent(pendingIntent)
    }

    // Status notification is ongoing for API 33 and below
    // Starting from API 34, users can disable the service by dismissing control notification
    private fun NotificationCompat.Builder.addOptionalDisableButton(): NotificationCompat.Builder {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(StringsR.string.notification_button_disable_service),
                PendingIntent.getBroadcast(
                    this@NotificationService,
                    0,
                    Intent(DISABLE_SERVICE_LOCAL_ACTION).setPackage(packageName),
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
                this@NotificationService,
                0,
                Intent(CANCEL_RECOGNITION_LOCAL_ACTION).setPackage(packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun NotificationCompat.Builder.addDismissIntent(): NotificationCompat.Builder {
        return setDeleteIntent(
            PendingIntent.getBroadcast(
                this@NotificationService,
                0,
                Intent(DISABLE_SERVICE_LOCAL_ACTION).setPackage(packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private suspend fun NotificationCompat.Builder.addOptionalBigPicture(
        url: String?,
        contentTitle: String,
        contentText: String
    ): NotificationCompat.Builder {
        if (url == null) return this
        // Images should be ≤ 450dp wide, 2:1 aspect ratio
        val imageWidthPx = dpToPx(450f).toInt()
        val imageHeightPx = imageWidthPx / 2
        val bitmap = getCachedImageOrNull(
            url = url,
            widthPx = imageWidthPx,
            heightPx = imageHeightPx,
        ) ?: return this
        return setStyle(
            NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .setBigContentTitle(contentTitle)
                .setSummaryText(contentText)
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        showBigPictureWhenCollapsed(true)
                    } else {
                        this
                    }
                }
        )
    }

    private fun NotificationCompat.Builder.addTrackDeepLinkIntent(
        trackId: String
    ): NotificationCompat.Builder {
        val deepLinkIntent = serviceRouter.getDeepLinkIntentToTrack(trackId)
        val pendingIntent = createPendingIntent(deepLinkIntent)
        return setContentIntent(pendingIntent)
    }

    private fun NotificationCompat.Builder.addOptionalShowLyricsButton(
        track: Track
    ): NotificationCompat.Builder {
        if (track.lyrics == null) return this
        val deepLinkIntent = serviceRouter.getDeepLinkIntentToLyrics(track.id)
        val pendingIntent = createPendingIntent(deepLinkIntent)
        return addAction(
            android.R.drawable.ic_menu_more,
            getString(StringsR.string.show_lyrics),
            pendingIntent
        )
    }

    private fun NotificationCompat.Builder.addShareButton(
        sharedText: String
    ): NotificationCompat.Builder {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sharedText)
        }
        val wrappedIntent = Intent.createChooser(intent, null)
        return addAction(
            android.R.drawable.ic_menu_share,
            getString(StringsR.string.share),
            PendingIntent.getActivity(
                this@NotificationService,
                0,
                wrappedIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun NotificationCompat.Builder.buildAndNotifyAsStatus() {
        notificationManager.notify(STATUS_NOTIFICATION_ID, this.build())
    }

    private fun NotificationCompat.Builder.buildAndNotifyAsResult() {
        notificationManager.notify(RESULT_NOTIFICATION_ID, this.build())
    }

    private fun launchRecognition() {
        if (isRecognitionJobActive) return
        if (isRecognitionCancelingJobActive) return
        if (recognitionInteractor.status.value != RecognitionStatus.Ready) return

        recognitionJob = serviceScope.launch {
            val foregroundStateSwitcher = launch {
                screenStatusHolder.isStatusObserving.collect { isScreenObserving ->
                    if (isHoldModeActive || !isScreenObserving) {
                        if (!isStartedForeground) continueInForeground()
                    } else {
                        if (isStartedForeground) continueInBackground()
                    }
                }
            }
            if (networkMonitor.isOffline.first()) {
                recognitionInteractor.launchOfflineRecognition(serviceScope)
            } else {
                recognitionInteractor.launchRecognition(serviceScope)
            }
            recognitionInteractor.status.transformWhile { status ->
                emit(status)
                status !is RecognitionStatus.Done
            }.collect { status ->
                when (status) {
                    RecognitionStatus.Ready -> {} // NO-OP

                    is RecognitionStatus.Recognizing -> {
                        // Cancel last result notification if it exists
                        notificationManager.cancel(RESULT_NOTIFICATION_ID)

                        screenStatusHolder.updateStatus(status)
                        widgetStatusHolder.updateStatus(status)
                        requestWidgetsUpdate()
                        requestQuickTileUpdate()

                        if (isStartedForeground) {
                            notifyRecognizing(status.extraTry)
                        }
                    }

                    is RecognitionStatus.Done -> {
                        if (status.result is RecognitionResult.Success) {
                            listOfNotNull(
                                status.result.track.artworkThumbUrl,
                                status.result.track.artworkUrl
                            ).map { imageUrl ->
                                async { downloadImageToDiskCache(imageUrl) }
                            }.awaitAll()
                        }
                        val isScreenUpdated = screenStatusHolder.updateStatusIfObserving(status)
                        if (isScreenUpdated) {
                            widgetStatusHolder.updateStatus(RecognitionStatus.Ready)
                        } else {
                            val resultBuilder = createResultNotification(status.result)
                            screenStatusHolder.updateStatus(RecognitionStatus.Ready)
                            if (hasActiveWidgets()) {
                                widgetStatusHolder.updateStatus(status)
                            } else {
                                widgetStatusHolder.updateStatus(RecognitionStatus.Ready)
                            }
                            resultBuilder.buildAndNotifyAsResult()
                        }
                        requestWidgetsUpdate()
                        requestQuickTileUpdate()
                        recognitionInteractor.resetFinalStatus()
                    }
                }
            }

            foregroundStateSwitcher.cancelAndJoin()
            if (isHoldModeActive) {
                notifyReady()
            } else {
                stopSelf()
            }
        }
    }

    private fun notifyReady() {
        statusNotificationBuilder()
            .setContentTitle(getString(StringsR.string.tap_to_recognize_short))
            .setContentText(getString(StringsR.string.identify_while_using_another_app_short))
            .addRecognizeContentIntent()
            .addOptionalDisableButton()
            .buildAndNotifyAsStatus()
    }

    private fun notifyRecognizing(extraTry: Boolean) {
        statusNotificationBuilder()
            .setContentTitle(getString(StringsR.string.listening))
            .setContentText(
                if (extraTry) {
                    getString(StringsR.string.trying_one_more_time)
                } else {
                    getString(StringsR.string.please_wait)
                }
            )
            .addCancelButton()
            .buildAndNotifyAsStatus()
    }

    private suspend fun createResultNotification(
        result: RecognitionResult
    ): NotificationCompat.Builder {
        return when (result) {
            is RecognitionResult.Error -> when (result.remoteError) {
                RemoteRecognitionResult.Error.BadConnection -> {
                    resultNotificationBuilder()
                        .setContentTitle(getString(StringsR.string.bad_internet_connection))
                        .setContentText(getString(StringsR.string.please_check_network_status))
                }

                is RemoteRecognitionResult.Error.BadRecording -> {
                    resultNotificationBuilder()
                        .setContentTitle(getString(StringsR.string.recording_error))
                        .setContentText(getString(StringsR.string.notification_message_recording_error))
                }

                is RemoteRecognitionResult.Error.HttpError -> {
                    resultNotificationBuilder()
                        .setContentTitle(getString(StringsR.string.bad_network_response))
                        .setContentText(getString(StringsR.string.message_http_error))
                }

                is RemoteRecognitionResult.Error.UnhandledError -> {
                    resultNotificationBuilder()
                        .setContentTitle(getString(StringsR.string.internal_error))
                        .setContentText(getString(StringsR.string.notification_message_unhandled_error))
                }

                is RemoteRecognitionResult.Error.AuthError -> {
                    resultNotificationBuilder()
                        .setContentTitle(getString(StringsR.string.auth_error))
                        .setContentText(getString(StringsR.string.auth_error_message))
                }

                is RemoteRecognitionResult.Error.ApiUsageLimited -> {
                    resultNotificationBuilder()
                        .setContentTitle(getString(StringsR.string.service_usage_limited))
                        .setContentText(getString(StringsR.string.service_usage_limited_message))
                }
            }

            is RecognitionResult.ScheduledOffline -> {
                resultNotificationBuilder()
                    .setContentTitle(getString(StringsR.string.recognition_scheduled))
                    .setContentText(
                        if (result.recognitionTask is RecognitionTask.Created) {
                            val extraMessage = if (result.recognitionTask.launched) {
                                getString(StringsR.string.auto_recognition_message_network)
                            } else {
                                getString(StringsR.string.manual_recognition_message)
                            }
                            getString(StringsR.string.saved_recording_message) + ", $extraMessage"
                        } else {
                            getString(StringsR.string.recognition_scheduled)
                        }
                    )
            }

            is RecognitionResult.NoMatches -> {
                resultNotificationBuilder()
                    .setContentTitle(getString(StringsR.string.no_matches_found))
                    .setContentText(getString(StringsR.string.no_matches_message))
            }

            is RecognitionResult.Success -> {
                resultNotificationBuilder()
                    .setContentTitle(result.track.title)
                    .setContentText(result.track.artist)
                    .addOptionalBigPicture(
                        url = result.track.artworkUrl,
                        contentTitle = result.track.title,
                        contentText = result.track.artistWithAlbumFormatted()
                    )
                    .addTrackDeepLinkIntent(result.track.id)
                    .addOptionalShowLyricsButton(result.track)
                    .addShareButton(result.track.getSharedBody())
            }
        }
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

    private fun cancelRecognitionJob() {
        recognitionCancelingJob = serviceScope.launch {
            recognitionJob?.cancelAndJoin()
            recognitionInteractor.cancelAndResetStatus()
            screenStatusHolder.updateStatus(RecognitionStatus.Ready)
            widgetStatusHolder.updateStatus(RecognitionStatus.Ready)
            requestWidgetsUpdate()
            requestQuickTileUpdate()
            if (isHoldModeActive) {
                notifyReady()
            } else {
                stopSelf()
            }
        }
    }

    private fun checkNotificationServicePermissions(): Boolean {
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
                CANCEL_RECOGNITION_LOCAL_ACTION -> {
                    cancelRecognitionJob()
                }

                DISABLE_SERVICE_LOCAL_ACTION -> {
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

    companion object {
        private const val NOTIFICATION_STATUS_CHANNEL_ID = "com.mrsep.musicrecognizer.status"
        private const val NOTIFICATION_RESULT_CHANNEL_ID = "com.mrsep.musicrecognizer.result"
        private const val STATUS_NOTIFICATION_ID = 1
        private const val RESULT_NOTIFICATION_ID = 2

        // Actions for local broadcasting
        private const val CANCEL_RECOGNITION_LOCAL_ACTION =
            "com.mrsep.musicrecognizer.service.action.local.cancel_recognition"
        private const val DISABLE_SERVICE_LOCAL_ACTION =
            "com.mrsep.musicrecognizer.service.action.local.disable_service"

        // See isMicrophoneRestricted
        const val KEY_RESTRICTED_START = "KEY_RESTRICTED_START"

        // True if the launch occurs from the background (widget, tile),
        // and false if the launch occurs from main activity
        const val KEY_FOREGROUND_REQUESTED = "KEY_FOREGROUND_REQUESTED"

        const val LAUNCH_RECOGNITION_ACTION =
            "com.mrsep.musicrecognizer.service.action.launch_recognition"
        const val CANCEL_RECOGNITION_ACTION =
            "com.mrsep.musicrecognizer.service.action.cancel_recognition"
        const val HOLD_MODE_ON_ACTION = "com.mrsep.musicrecognizer.service.action.hold_mode_on"
        const val HOLD_MODE_OFF_ACTION = "com.mrsep.musicrecognizer.service.action.hold_mode_off"

        fun cancelResultNotification(context: Context) {
            with(context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
                cancel(RESULT_NOTIFICATION_ID)
            }
        }

        fun createStatusNotificationChannel(context: Context) {
            val name = context.getString(StringsR.string.notification_channel_name_control)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(NOTIFICATION_STATUS_CHANNEL_ID, name, importance).apply {
                    description =
                        context.getString(StringsR.string.notification_channel_desc_control)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(false)
                    enableLights(false)
                    enableVibration(false)
                }
            with(context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
                createNotificationChannel(channel)
            }
        }

        fun createResultNotificationChannel(context: Context) {
            val name = context.getString(StringsR.string.notification_channel_name_result)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(NOTIFICATION_RESULT_CHANNEL_ID, name, importance).apply {
                    description =
                        context.getString(StringsR.string.notification_channel_desc_result)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(true)
                    enableLights(true)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(100, 100, 100, 100)
                }
            with(context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
                createNotificationChannel(channel)
            }
        }
    }
}
