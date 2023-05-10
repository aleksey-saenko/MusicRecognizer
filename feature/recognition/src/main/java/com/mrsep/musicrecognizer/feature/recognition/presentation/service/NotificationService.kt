package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.common.di.MainDispatcher
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueueRecognitionUseCase
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val SERVICE_TAG = "NotificationService"

@AndroidEntryPoint
internal class NotificationService : Service() {

    @Inject lateinit var recognitionInteractor: ServiceRecognitionInteractor
    @Inject lateinit var enqueueRecognitionUseCase: EnqueueRecognitionUseCase
    @Inject lateinit var trackRepository: TrackRepository
    @Inject lateinit var serviceRouter: NotificationServiceRouter

    @Inject @MainDispatcher lateinit var mainDispatcher: CoroutineDispatcher
    @Inject @IoDispatcher lateinit var ioDispatcher: CoroutineDispatcher
    @Inject @DefaultDispatcher lateinit var defaultDispatcher: CoroutineDispatcher

    private val actionReceiver = ActionBroadcastReceiver()
    private val serviceScope by lazy { CoroutineScope(ioDispatcher + SupervisorJob()) }
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    private val recognitionState get() = recognitionInteractor.serviceRecognitionStatus

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        Log.d(SERVICE_TAG, "onCreate")
        super.onCreate()
        createStatusNotificationChannel()
        createResultNotificationChannel()
        val initialNotification = statusNotificationBuilder()
            .setContentTitle(getString(StringsR.string.tap_to_recognize))
            .addRecognizeButton().addRecognizeIntent().build()
        startForeground(STATUS_NOTIFICATION_ID, initialNotification)
        ContextCompat.registerReceiver(
            this,
            actionReceiver,
            IntentFilter().apply {
                addAction(RECOGNIZE_ACTION)
                addAction(CANCEL_ACTION)
                addAction(DISMISS_ACTION)
                addAction(SHARE_TRACK_ACTION)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        recognitionState.onEach { status ->
            if (status is RecognitionStatus.Done &&
                status.result is RecognitionResult.Error
            ) {
                enqueueRecognitionUseCase(
                    audioRecording = status.result.audioRecording,
                    launch = true
                )
            }
            handleRecognitionStatus(status)
        }.launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SERVICE_TAG, "onStartCommand: flags=$flags, startId=$startId")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(SERVICE_TAG, "onDestroy")
        serviceScope.cancel()
        unregisterReceiver(actionReceiver)
        super.onDestroy()
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
                statusNotificationBuilder()
                    .setContentText(getString(StringsR.string.tap_to_recognize))
                    .addRecognizeButton()
                    .addRecognizeIntent()
                    .buildAndNotifyAsStatus()

                notificationManager.cancel(RESULT_NOTIFICATION_ID)
            }
            is RecognitionStatus.Recognizing -> { //TODO() add extra try message
                statusNotificationBuilder()
                    .setContentTitle(getString(StringsR.string.listening))
                    .addCancelButton()
                    .buildAndNotifyAsStatus()
            }

            is RecognitionStatus.Done -> {
                when (status.result) {
                    is RecognitionResult.Error -> when (status.result.remoteError) {
                        RemoteRecognitionResult.Error.BadConnection -> {
                            resultNotificationBuilder()
                                .setContentTitle(getString(StringsR.string.no_internet_connection))
                                .setContentText(getString(StringsR.string.notification_message_internet_not_available))
                                .addDismissIntent()
                                .buildAndNotifyAsResult()

                            notifyReadyAsStatus()
                        }

                        RemoteRecognitionResult.Error.BadRecording -> {
                            resultNotificationBuilder()
                                .setContentTitle(getString(StringsR.string.recording_error))
                                .setContentText(getString(StringsR.string.notification_message_recording_error))
                                .addDismissIntent()
                                .buildAndNotifyAsResult()

                            notifyReadyAsStatus()
                        }

                        is RemoteRecognitionResult.Error.HttpError,
                        is RemoteRecognitionResult.Error.UnhandledError -> {
                            resultNotificationBuilder()
                                .setContentTitle(getString(StringsR.string.internal_error))
                                .setContentText(getString(StringsR.string.notification_message_unhandled_error))
                                .addDismissIntent()
                                .buildAndNotifyAsResult()

                            notifyReadyAsStatus()
                        }

                        is RemoteRecognitionResult.Error.WrongToken -> {
                            resultNotificationBuilder()
                                .setContentTitle(
                                    if (status.result.remoteError.isLimitReached)
                                        getString(StringsR.string.token_limit_reached)
                                    else
                                        getString(StringsR.string.wrong_token)
                                )
                                .setContentText(getString(StringsR.string.notification_message_token_wrong_error))
                                .addDismissIntent()
                                .buildAndNotifyAsResult()

                            notifyReadyAsStatus()
                        }
                    }

                    is RecognitionResult.NoMatches -> {
                        resultNotificationBuilder()
                            .setContentTitle(getString(StringsR.string.no_matches_found))
                            .setContentText(getString(StringsR.string.notification_message_no_matches_found))
                            .addQueueButton()
                            .addDismissIntent()
                            .buildAndNotifyAsResult()

                        notifyReadyAsStatus()
                    }

                    is RecognitionResult.Success -> {
                        resultNotificationBuilder()
                            .setContentTitle(status.result.track.title)
                            .setContentText(status.result.track.artistWithAlbumFormatted())
                            .addBigPicture(status.result.track.links.artwork)
                            .addTrackDeepLinkIntent(status.result.track.mbId)
                            .addDismissIntent()
                            .addShareButton(status.result.track)
                            .buildAndNotifyAsResult()

                        notifyReadyAsStatus()
                    }
                }
            }
        }
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

    private fun notifyReadyAsStatus() {
        statusNotificationBuilder()
            .setContentText(getString(StringsR.string.tap_to_recognize))
            .addRecognizeButton()
            .addRecognizeIntent()
            .buildAndNotifyAsStatus()
    }

    private fun statusNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_STATUS_CHANNEL_ID)
            .setContentTitle(getString(StringsR.string.app_name))
            .setSmallIcon(UiR.drawable.baseline_album_24)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
//            .setAutoCancel(false)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_STATUS)
//        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
    }


    private fun resultNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_RESULT_CHANNEL_ID)
            .setSmallIcon(UiR.drawable.baseline_album_24)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
//            .setAutoCancel(false)
            .setOngoing(false)
            .setCategory(Notification.CATEGORY_MESSAGE)
//        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun NotificationCompat.Builder.addRecognizeIntent(): NotificationCompat.Builder {
        return setContentIntent(
            PendingIntent.getBroadcast(
                this@NotificationService,
                0,
                Intent(RECOGNIZE_ACTION),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun NotificationCompat.Builder.addRecognizeButton(): NotificationCompat.Builder {
        return addAction(
            android.R.drawable.ic_menu_search,
            getString(StringsR.string.recognize),
            PendingIntent.getBroadcast(
                this@NotificationService,
                0,
                Intent(RECOGNIZE_ACTION),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun NotificationCompat.Builder.addCancelButton(): NotificationCompat.Builder {
        return addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            getString(StringsR.string.cancel),
            PendingIntent.getBroadcast(
                this@NotificationService,
                0,
                Intent(CANCEL_ACTION),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun NotificationCompat.Builder.addQueueButton(): NotificationCompat.Builder {
        return addAction(
            android.R.drawable.ic_menu_preferences,
            getString(StringsR.string.recognition_queue),
            createQueueDeepLinkIntent()
        )
    }

    private fun NotificationCompat.Builder.addDismissIntent(): NotificationCompat.Builder {
        return setDeleteIntent(
            PendingIntent.getBroadcast(
                this@NotificationService,
                0,
                Intent(DISMISS_ACTION),
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

    private suspend fun NotificationCompat.Builder.addBigPicture(url: String?): NotificationCompat.Builder {
        return url?.let {
            getCoilBitmapOrNull(url)?.let { bitmap ->
                setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                )
            }
        } ?: this
    }

    private suspend fun getCoilBitmapOrNull(url: String): Bitmap? {
        return withContext(ioDispatcher) {
            val context = this@NotificationService
            val request = ImageRequest.Builder(context).data(url)
                .allowHardware(false).build()
            when (val result = context.imageLoader.execute(request)) {
                is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
                is ErrorResult -> null
            }
        }
    }

    private fun resetStatusToReady() {
        recognitionInteractor.cancelAndResetStatus()
    }

    private fun cancelRecognition() {
        recognitionInteractor.cancelAndResetStatus()
    }

    private fun launchRecognizeOrCancel() {
        if (recognitionState.value is RecognitionStatus.Recognizing) {
            recognitionInteractor.cancelAndResetStatus()
        } else {
            recognitionInteractor.launchRecognition(serviceScope)
        }
    }

    private fun NotificationCompat.Builder.addTrackDeepLinkIntent(mbId: String): NotificationCompat.Builder {
        val deepLinkIntent = serviceRouter.getDeepLinkIntentToTrack(mbId)

        val pendingIntent = TaskStackBuilder.create(this@NotificationService).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }
        return setContentIntent(pendingIntent)
    }

    private fun createQueueDeepLinkIntent(): PendingIntent {
        val deepLinkIntent = serviceRouter.getDeepLinkIntentToRecognitionQueue()

        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }
    }


    private inner class ActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                RECOGNIZE_ACTION -> {
                    launchRecognizeOrCancel()
                }

                CANCEL_ACTION -> {
                    cancelRecognition()
                }

                DISMISS_ACTION -> {
                    resetStatusToReady()
                }

                SHARE_TRACK_ACTION -> {
                    TODO()
                }
            }
        }
    }

    private fun NotificationCompat.Builder.addShareButton(track: Track): NotificationCompat.Builder {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, track.getSharedBody())
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

    companion object {
        private const val NOTIFICATION_STATUS_CHANNEL_ID = "music_recognizer_notification_ch_status"
        private const val NOTIFICATION_RESULT_CHANNEL_ID = "music_recognizer_notification_ch_result"
        private const val STATUS_NOTIFICATION_ID = 1
        private const val RESULT_NOTIFICATION_ID = 2

        private const val RECOGNIZE_ACTION = "com.mrsep.musicrecognizer.action.recognize"
        private const val CANCEL_ACTION = "com.mrsep.musicrecognizer.action.cancel"
        private const val DISMISS_ACTION = "com.mrsep.musicrecognizer.action.dismiss_notification"
        private const val SHARE_TRACK_ACTION = "com.mrsep.musicrecognizer.action.share_track"
    }

}

fun Context.toggleNotificationService(shouldStart: Boolean) {
    if (shouldStart) {
        startForegroundService(Intent(this, NotificationService::class.java))
    } else {
        stopService(Intent(this, NotificationService::class.java))
    }
}

