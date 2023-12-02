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
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.feature.recognition.domain.NetworkMonitor
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import com.mrsep.musicrecognizer.feature.recognition.presentation.ext.artistWithAlbumFormatted
import com.mrsep.musicrecognizer.feature.recognition.presentation.ext.fetchBitmapOrNull
import com.mrsep.musicrecognizer.feature.recognition.presentation.ext.getSharedBody
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.lang.IllegalArgumentException
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

/**
 * When designing behavior, consider these restrictions:
 * https://issuetracker.google.com/issues/36961721
 * https://developer.android.com/guide/components/activities/background-starts
 * https://developer.android.com/about/versions/12/behavior-changes-12#notification-trampolines
 * https://developer.android.com/guide/components/foreground-services#bg-access-restrictions
 */

@AndroidEntryPoint
class NotificationService : Service() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository
    @Inject
    lateinit var recognitionInteractor: ServiceRecognitionInteractor
    @Inject
    lateinit var serviceRouter: NotificationServiceRouter
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    private val serviceScope by lazy { CoroutineScope(ioDispatcher + SupervisorJob()) }

    private val actionReceiver = ActionBroadcastReceiver()

    private val notificationManager by lazy { getNotificationManager() }

    private val recognitionState get() = recognitionInteractor.serviceRecognitionStatus
    private val isReadyToRecognize get() = recognitionState.value is RecognitionStatus.Ready

    private var notificationSendingJob: Job? = null

    private var oneTimeMode = true
    private var oneTimeRecognitionLaunched = false
    // can be used to design background startup behaviour, not used now
    private var microphoneRestricted = true

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate() // required by hilt injection
        val initialNotification = statusNotificationBuilder()
            .setContentText(getString(StringsR.string.notification_service_initializing))
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check again because user can restrict permissions when the app is in the background,
            // which can lead to service restarting without required permissions
            val startAllowed = checkNotificationServicePermissions()
            if (startAllowed) {
                startForeground(
                    STATUS_NOTIFICATION_ID,
                    initialNotification,
                    FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
                registerActionReceiver()
            } else {
                serviceScope.launch {
                    preferencesRepository.setNotificationServiceEnabled(false)
                    stopSelf()
                }
            }
        } else {
            startForeground(STATUS_NOTIFICATION_ID, initialNotification)
            registerActionReceiver()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        var oneTimeRequested = false
        intent?.run {
            microphoneRestricted = getBooleanExtra(KEY_BACKGROUND_LAUNCH, true)
            oneTimeRequested = getBooleanExtra(KEY_ONE_TIME_RECOGNITION, false)
        }

        if (oneTimeRequested) {
            if (isReadyToRecognize) {
                notificationSendingJob ?: registerRecognitionStateObserver()
                launchRecognition()
            } else {
                cancelAndResetStatus()
            }
        } else {
            notificationSendingJob ?: registerRecognitionStateObserver()
            oneTimeMode = false
        }

        return if (oneTimeMode) START_NOT_STICKY else START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(actionReceiver)
        } catch (_: IllegalArgumentException) {
            // receiver was not registered, no-op
        }
        serviceScope.cancel()
    }

    private fun registerActionReceiver() {
        ContextCompat.registerReceiver(
            this,
            actionReceiver,
            IntentFilter().apply {
                addAction(RECOGNIZE_ACTION)
                addAction(CANCEL_RECOGNITION_ACTION)
                addAction(DISMISS_STATUS_ACTION)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun registerRecognitionStateObserver() {
        notificationSendingJob = serviceScope.launch {
            recognitionState.collectLatest(::handleRecognitionStatus)
        }
    }

    private suspend fun handleRecognitionStatus(status: RecognitionStatus) {
        when (status) {
            RecognitionStatus.Ready -> {
                if (oneTimeMode) {
                    if (oneTimeRecognitionLaunched) stopSelf()
                } else {
                    statusNotificationBuilder()
                        .setContentText(getString(StringsR.string.tap_to_recognize_the_song))
                        .addRecognizeButtonAndIntent()
                        .buildAndNotifyAsStatus()
                }
            }

            is RecognitionStatus.Recognizing -> {
                oneTimeRecognitionLaunched = true
                // cancel last result notification if it exists
                // cancel microphone permission notification if it exists
                // TODO: consider to create a channel for warnings
                notificationManager.cancel(RESULT_NOTIFICATION_ID)
                statusNotificationBuilder()
                    .setContentText(getListeningMessage(status.extraTry))
                    .addCancelButton()
                    .buildAndNotifyAsStatus()
            }

            is RecognitionStatus.Done -> {
                val resultBuilder = when (status.result) {
                    is RecognitionResult.Error -> when (status.result.remoteError) {
                        RemoteRecognitionResult.Error.BadConnection -> {
                            resultNotificationBuilder()
                                .setContentTitle(getString(StringsR.string.bad_internet_connection))
                                .setContentText(getString(StringsR.string.please_check_network_status))
                                .addOptionalQueueButton(status.result.recognitionTask)
                        }

                        is RemoteRecognitionResult.Error.BadRecording -> {
                            resultNotificationBuilder()
                                .setContentTitle(getString(StringsR.string.recording_error))
                                .setContentText(getString(StringsR.string.notification_message_recording_error))
                        }

                        is RemoteRecognitionResult.Error.HttpError,
                        is RemoteRecognitionResult.Error.UnhandledError -> {
                            resultNotificationBuilder()
                                .setContentTitle(getString(StringsR.string.internal_error))
                                .setContentText(getString(StringsR.string.notification_message_unhandled_error))
                                .addOptionalQueueButton(status.result.recognitionTask)
                        }

                        is RemoteRecognitionResult.Error.WrongToken -> {
                            resultNotificationBuilder()
                                .setContentTitle(getWrongTokenTitle(status.result.remoteError.isLimitReached))
                                .setContentText(getString(StringsR.string.message_token_wrong_error))
                                .addOptionalQueueButton(status.result.recognitionTask)
                        }
                    }

                    is RecognitionResult.ScheduledOffline -> {
                        resultNotificationBuilder()
                            .setContentTitle(getString(StringsR.string.recognition_scheduled))
                            .addOptionalQueueButton(status.result.recognitionTask)
                    }

                    is RecognitionResult.NoMatches -> {
                        resultNotificationBuilder()
                            .setContentTitle(getString(StringsR.string.no_matches_found))
                            .setContentText(getString(StringsR.string.no_matches_message))
                            .addOptionalQueueButton(status.result.recognitionTask)
                    }

                    is RecognitionResult.Success -> {
                        resultNotificationBuilder()
                            .setContentTitle(status.result.track.title)
                            .setContentText(status.result.track.artistWithAlbumFormatted())
                            .addOptionalBigPicture(status.result.track.artworkUrl)
                            .addTrackDeepLinkIntent(status.result.track.mbId)
                            .addOptionalShowLyricsButton(status.result.track)
                            .addShareButton(status.result.track.getSharedBody())
                    }
                }
                resultBuilder.buildAndNotifyAsResult()
                cancelAndResetStatus()
            }
        }
    }

    private fun getWrongTokenTitle(isLimitReached: Boolean): String {
        return if (isLimitReached) {
            getString(StringsR.string.token_limit_reached)
        } else {
            getString(StringsR.string.wrong_token)
        }
    }

    private fun getListeningMessage(extraTry: Boolean): String {
        return if (extraTry) {
            getString(StringsR.string.listening_last_attempt)
        } else {
            getString(StringsR.string.listening_with_ellipsis)
        }
    }

    private fun createPendingIntent(intent: Intent): PendingIntent {
        return TaskStackBuilder.create(this@NotificationService).run {
            addNextIntentWithParentStack(intent)
            // FLAG_UPDATE_CURRENT to update track mbId key on each result
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
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun NotificationCompat.Builder.addAppPreferencesButtonAndIntent(): NotificationCompat.Builder {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        val pendingIntent = createPendingIntent(appSettingsIntent)
        return addAction(
            android.R.drawable.ic_menu_preferences,
            getString(StringsR.string.open_settings),
            pendingIntent
        ).setContentIntent(pendingIntent)
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun NotificationCompat.Builder.addRecognizeButtonAndIntent(): NotificationCompat.Builder {
//        can be implemented by transparent activity to grant microphone access,
//        but it leads to minimizing previous launched app

//        val mediateActivityIntent = Intent(
//            this@NotificationService,
//            NotificationServiceActivity::class.java
//        ).apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            action = RECOGNIZE_ACTION
//        }
//        val pendingIntent = createPendingIntent(mediateActivityIntent)
//        return addAction(
//            android.R.drawable.ic_menu_search,
//            getString(StringsR.string.recognize),
//            pendingIntent
//        ).setContentIntent(pendingIntent)
        val pendingIntent = PendingIntent.getBroadcast(
            this@NotificationService,
            0,
            Intent(RECOGNIZE_ACTION).setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        return addAction(
            android.R.drawable.ic_menu_search,
            getString(StringsR.string.recognize),
            pendingIntent
        ).setContentIntent(pendingIntent)
    }

    private fun NotificationCompat.Builder.addCancelButton(): NotificationCompat.Builder {
        return addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            getString(StringsR.string.cancel),
            PendingIntent.getBroadcast(
                this@NotificationService,
                0,
                Intent(CANCEL_RECOGNITION_ACTION).setPackage(packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun NotificationCompat.Builder.addOptionalQueueButton(
        task: RecognitionTask
    ): NotificationCompat.Builder {
        return when (task) {
            is RecognitionTask.Created -> {
                val deepLinkIntent = serviceRouter.getDeepLinkIntentToRecognitionQueue()
                val pendingIntent = createPendingIntent(deepLinkIntent)
                return addAction(
                    android.R.drawable.ic_menu_preferences,
                    getString(StringsR.string.recognition_queue),
                    pendingIntent
                )
            }

            is RecognitionTask.Error,
            RecognitionTask.Ignored -> this
        }
    }

    private fun NotificationCompat.Builder.addDismissIntent(): NotificationCompat.Builder {
        return setDeleteIntent(
            PendingIntent.getBroadcast(
                this@NotificationService,
                0,
                Intent(DISMISS_STATUS_ACTION).setPackage(packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private suspend fun NotificationCompat.Builder.addOptionalBigPicture(
        url: String?
    ): NotificationCompat.Builder {
        return url?.let {
            this@NotificationService.fetchBitmapOrNull(url)?.let { bitmap ->
                setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                )
            }
        } ?: this
    }

    private fun NotificationCompat.Builder.addTrackDeepLinkIntent(
        mbId: String
    ): NotificationCompat.Builder {
        val deepLinkIntent = serviceRouter.getDeepLinkIntentToTrack(mbId)
        val pendingIntent = createPendingIntent(deepLinkIntent)
        return setContentIntent(pendingIntent)
    }

    private fun NotificationCompat.Builder.addOptionalShowLyricsButton(
        track: Track
    ): NotificationCompat.Builder {
        if (track.lyrics == null) return this
        val deepLinkIntent = serviceRouter.getDeepLinkIntentToLyrics(track.mbId)
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
        serviceScope.launch {
            if (networkMonitor.isOffline.first()) {
                recognitionInteractor.launchOfflineRecognition(this)
            } else {
                recognitionInteractor.launchRecognition(this)
            }
        }
    }

    private fun cancelAndResetStatus() {
        if (oneTimeMode) {
            notificationSendingJob?.cancel()
            recognitionInteractor.cancelAndResetStatus()
            stopSelf()
        } else {
            recognitionInteractor.cancelAndResetStatus()
        }
    }

    private inner class ActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                RECOGNIZE_ACTION -> {
                    val permissionGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (permissionGranted == PackageManager.PERMISSION_GRANTED) {
                        if (isReadyToRecognize) {
                            launchRecognition()
                        } else {
                            cancelAndResetStatus()
                        }
                    } else {
                        serviceScope.launch {
                            // wait for the end of notification drawer closing animation
                            // to see notification bubble
                            val drawerAnimationDuration =
                                resources.getInteger(android.R.integer.config_longAnimTime)
                            delay(drawerAnimationDuration.toLong())
                            resultNotificationBuilder()
                                .setContentTitle(context.getString(StringsR.string.permissions))
                                .setContentText(getString(StringsR.string.allow_the_app_to_access_the_microphone))
                                .setAutoCancel(true)
                                .addAppPreferencesButtonAndIntent()
                                .buildAndNotifyAsResult()
                        }
                    }
                }

                CANCEL_RECOGNITION_ACTION -> {
                    cancelAndResetStatus()
                }

                DISMISS_STATUS_ACTION -> {
                    serviceScope.launch {
                        preferencesRepository.setNotificationServiceEnabled(false)
                        stopSelf()
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

        const val RECOGNIZE_ACTION = "com.mrsep.musicrecognizer.action.recognize"
        const val CANCEL_RECOGNITION_ACTION = "com.mrsep.musicrecognizer.action.cancel_recognition"
        const val DISMISS_STATUS_ACTION = "com.mrsep.musicrecognizer.action.dismiss_notification"
        const val SHOW_TRACK_ACTION = "com.mrsep.musicrecognizer.action.show_track"
        const val SHOW_LYRICS_ACTION = "com.mrsep.musicrecognizer.action.show_lyrics"

        const val KEY_BACKGROUND_LAUNCH = "KEY_BACKGROUND_LAUNCH"
        const val KEY_ONE_TIME_RECOGNITION = "KEY_ONE_TIME_RECOGNITION"

        const val MB_ID_EXTRA_KEY = "KEY_MB_ID"


        fun createStatusNotificationChannel(context: Context) {
            val name = context.getString(StringsR.string.notification_channel_name_control)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_STATUS_CHANNEL_ID, name, importance).apply {
                description = context.getString(StringsR.string.notification_channel_desc_control)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            context.getNotificationManager().createNotificationChannel(channel)
        }

        fun createResultNotificationChannel(context: Context) {
            val name = context.getString(StringsR.string.notification_channel_name_result)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_RESULT_CHANNEL_ID, name, importance).apply {
                description = context.getString(StringsR.string.notification_channel_desc_result)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 100, 100, 100)
            }
            context.getNotificationManager().createNotificationChannel(channel)
        }
    }

}

fun Context.startNotificationService(
    backgroundLaunch: Boolean = false,
    oneTimeRecognition: Boolean = false
) {
    val intent = Intent(this, NotificationService::class.java).apply {
        putExtra(NotificationService.KEY_BACKGROUND_LAUNCH, backgroundLaunch)
        putExtra(NotificationService.KEY_ONE_TIME_RECOGNITION, oneTimeRecognition)
    }
    startForegroundService(intent)
}

fun Context.stopNotificationService() {
    stopService(Intent(this, NotificationService::class.java))
}

private fun Context.getNotificationManager() =
    getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

private fun Context.checkNotificationServicePermissions(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED)
    } else {
        (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED)
    }
}