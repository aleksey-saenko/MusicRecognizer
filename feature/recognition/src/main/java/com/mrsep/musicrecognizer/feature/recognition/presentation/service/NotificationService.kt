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
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.feature.recognition.domain.NetworkMonitor
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val SERVICE_TAG = "NotificationService"

/**
 * When designing behavior, consider these restrictions:
 * https://issuetracker.google.com/issues/36961721
 * https://developer.android.com/guide/components/activities/background-starts
 * https://developer.android.com/about/versions/12/behavior-changes-12#notification-trampolines
 * https://developer.android.com/guide/components/foreground-services#bg-access-restrictions
 */

@AndroidEntryPoint
internal class NotificationService : Service() {

    @Inject lateinit var recognitionInteractor: ServiceRecognitionInteractor
    @Inject lateinit var serviceRouter: NotificationServiceRouter
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject @IoDispatcher lateinit var ioDispatcher: CoroutineDispatcher

    private val actionReceiver = ActionBroadcastReceiver()
    private val serviceScope by lazy { CoroutineScope(ioDispatcher + SupervisorJob()) }
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    private lateinit var isOffline: StateFlow<Boolean>
    private val recognitionState get() = recognitionInteractor.serviceRecognitionStatus

    // can be used to design background startup behaviour, not used now
    private var microphoneRestricted = true

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        Log.d(SERVICE_TAG, "onCreate")
        super.onCreate() // required by hilt injection
        createStatusNotificationChannel()
        createResultNotificationChannel()
        val initialNotification = statusNotificationBuilder()
            .setContentTitle(getString(StringsR.string.notification_service_initializing))
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                STATUS_NOTIFICATION_ID,
                initialNotification,
                FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(STATUS_NOTIFICATION_ID, initialNotification)
        }

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

        isOffline = networkMonitor.isOffline.stateIn(
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

        isOffline.onEach { offline ->
            if (recognitionState.value is RecognitionStatus.Ready) {
                notifyReadyAsStatus(offline)
            }
        }.launchIn(serviceScope)

        recognitionState.onEach { status ->
            handleRecognitionStatus(status)
        }.launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(SERVICE_TAG, "onStartCommand: flags=$flags, startId=$startId")
        intent?.run {
            microphoneRestricted = getBooleanExtra(KEY_BACKGROUND_LAUNCH, true)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(SERVICE_TAG, "onDestroy")
        super.onDestroy()
        unregisterReceiver(actionReceiver)
        serviceScope.cancel()
    }

    private fun createStatusNotificationChannel() {
        val name = getString(StringsR.string.service_notification_main_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_STATUS_CHANNEL_ID, name, importance).apply {
            description = getString(StringsR.string.service_notification_main_channel_desc)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            vibrationPattern = longArrayOf(100, 100, 100, 100)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createResultNotificationChannel() {
        val name = getString(StringsR.string.service_notification_result_channel_name)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NOTIFICATION_RESULT_CHANNEL_ID, name, importance).apply {
            description = getString(StringsR.string.service_notification_result_channel_desc)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            enableLights(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(100, 100, 100, 100)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun handleRecognitionStatus(status: RecognitionStatus) {
        when (status) {
            RecognitionStatus.Ready -> {
                // cancel last final result notification if it exists
                notificationManager.cancel(RESULT_NOTIFICATION_ID)
                notifyReadyAsStatus(isOffline.value)
            }

            is RecognitionStatus.Recognizing -> {
                // cancel notification about microphone permission if it exists
                notificationManager.cancel(RESULT_NOTIFICATION_ID)

                statusNotificationBuilder()
                    .setContentText(getListeningMessage(status.extraTry))
                    .addCancelButton()
                    .buildAndNotifyAsStatus()
            }

            is RecognitionStatus.Done -> {
                val builder = when (status.result) {
                    is RecognitionResult.Error -> when (status.result.remoteError) {
                        RemoteRecognitionResult.Error.BadConnection -> {
                            resultNotificationBuilder()
                                .setContentTitle(getString(StringsR.string.no_internet_connection))
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
                                .setContentText(getString(StringsR.string.notification_message_token_wrong_error))
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
                            .addBigPicture(status.result.track.links.artwork)
                            .addTrackDeepLinkIntent(status.result.track.mbId)
                            .addShowLyricsButton(status.result.track.mbId)
                            .addShareButton(status.result.track.getSharedBody())
                    }
                }
                builder.addDismissIntent().buildAndNotifyAsResult()
                notifyReadyAsStatus(isOffline.value)
                // interactor still has final status, will be reset by notification dismiss
            }
        }
    }

    private fun getWrongTokenTitle(isLimitReached: Boolean): String {
        return if (isLimitReached)
            getString(StringsR.string.token_limit_reached)
        else
            getString(StringsR.string.wrong_token)
    }

    private fun getListeningMessage(extraTry: Boolean): String {
        return if (extraTry)
            getString(StringsR.string.listening_with_last_try)
        else
            getString(StringsR.string.listening_with_ellipsis)
    }

    private fun Track.artistWithAlbumFormatted(): String {
        val albumAndYear = album?.let { alb ->
            releaseDate?.year?.let { year -> "$alb ($year)" } ?: album
        }
        return albumAndYear?.let { albAndYear ->
            "$artist / $albAndYear"
        } ?: artist
    }

    private fun Track.getSharedBody() = "$title / ${this.artistWithAlbumFormatted()}"


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
            .setContentTitle(getString(StringsR.string.app_name))
            .setSmallIcon(UiR.drawable.baseline_album_24)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_STATUS)
//        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
    }


    private fun resultNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_RESULT_CHANNEL_ID)
            .setSmallIcon(UiR.drawable.baseline_album_24)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            // service should detect dismiss action and reset interactor status
            .setAutoCancel(false)
            .setOngoing(false)
            .setCategory(Notification.CATEGORY_MESSAGE)
//        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
    }

    private fun notifyReadyAsStatus(isOffline: Boolean) {
        val message = if (isOffline)
            getString(StringsR.string.tap_to_schedule_song_recognition)
        else
            getString(StringsR.string.tap_to_recognize_the_song)
        statusNotificationBuilder()
            .setContentText(message)
            .addRecognizeButtonAndIntent()
            .buildAndNotifyAsStatus()
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
            Intent(RECOGNIZE_ACTION),
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
                Intent(CANCEL_RECOGNITION_ACTION),
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
                Intent(DISMISS_STATUS_ACTION),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private suspend fun getCoilBitmapOrNull(url: String): Bitmap? {
        val context = this@NotificationService
        val request = ImageRequest.Builder(context).data(url)
            .allowHardware(false).build()
        return when (val result = context.imageLoader.execute(request)) {
            is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
            is ErrorResult -> null
        }
    }

    private suspend fun NotificationCompat.Builder.addBigPicture(url: String?): NotificationCompat.Builder {
        return url?.let {
            getCoilBitmapOrNull(url)?.let { bitmap ->
                setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                )
            }
        } ?: this
    }

    private fun NotificationCompat.Builder.addTrackDeepLinkIntent(
        mbId: String
    ): NotificationCompat.Builder {
        val mediateActivityIntent = Intent(
            this@NotificationService,
            NotificationServiceActivity::class.java
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = SHOW_TRACK_ACTION
            putExtra(MB_ID_EXTRA_KEY, mbId)
        }
        val pendingIntent = createPendingIntent(mediateActivityIntent)
        return setContentIntent(pendingIntent)
    }

    private fun NotificationCompat.Builder.addShowLyricsButton(
        mbId: String
    ): NotificationCompat.Builder {
        val mediateActivityIntent = Intent(
            this@NotificationService,
            NotificationServiceActivity::class.java
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = SHOW_LYRICS_ACTION
            putExtra(MB_ID_EXTRA_KEY, mbId)
        }
        val pendingIntent = createPendingIntent(mediateActivityIntent)
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

    private fun cancelAndResetStatus() {
        recognitionInteractor.cancelAndResetStatus()
    }

    private fun launchRecognizeOrCancel() {
        if (recognitionState.value is RecognitionStatus.Recognizing) {
            recognitionInteractor.cancelAndResetStatus()
        } else {
            if (isOffline.value) {
                recognitionInteractor.launchOfflineRecognition(serviceScope)
            } else {
                recognitionInteractor.launchRecognition(serviceScope)
            }
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
                        launchRecognizeOrCancel()
                    } else {
                        serviceScope.launch {
                            // wait for the end of notification drawer closing animation
                            // to see notification bubble
                            val drawerAnimationDuration =
                                resources.getInteger(android.R.integer.config_longAnimTime)
                            delay(drawerAnimationDuration.toLong())
                            resultNotificationBuilder()
                                .setContentTitle(context.getString(StringsR.string.app_name))
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
                    cancelAndResetStatus()
                }

            }
        }
    }

    companion object {
        private const val NOTIFICATION_STATUS_CHANNEL_ID = "music_recognizer_notification_ch_status"
        private const val NOTIFICATION_RESULT_CHANNEL_ID = "music_recognizer_notification_ch_result"
        private const val STATUS_NOTIFICATION_ID = 1
        private const val RESULT_NOTIFICATION_ID = 2

        const val RECOGNIZE_ACTION = "com.mrsep.musicrecognizer.action.recognize"
        const val CANCEL_RECOGNITION_ACTION = "com.mrsep.musicrecognizer.action.cancel_recognition"
        const val DISMISS_STATUS_ACTION = "com.mrsep.musicrecognizer.action.dismiss_notification"
        const val SHOW_TRACK_ACTION = "com.mrsep.musicrecognizer.action.show_track"
        const val SHOW_LYRICS_ACTION = "com.mrsep.musicrecognizer.action.show_lyrics"

        const val KEY_BACKGROUND_LAUNCH = "KEY_BACKGROUND_LAUNCH"

        const val MB_ID_EXTRA_KEY = "KEY_MB_ID"
    }

}

fun Context.toggleNotificationService(shouldStart: Boolean, backgroundLaunch: Boolean = false) {
    val intent = Intent(this, NotificationService::class.java).apply {
        putExtra(NotificationService.KEY_BACKGROUND_LAUNCH, backgroundLaunch)
    }
    if (shouldStart) startForegroundService(intent) else stopService(intent)
}

