package com.mrsep.musicrecognizer.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.di.DefaultDispatcher
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.di.MainDispatcher
import com.mrsep.musicrecognizer.domain.RecognizeInteractor
import com.mrsep.musicrecognizer.domain.RecognizeStatus
import com.mrsep.musicrecognizer.domain.TrackRepository
import com.mrsep.musicrecognizer.presentation.MainActivity
import com.mrsep.musicrecognizer.presentation.screens.track.Screen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.net.URL
import javax.inject.Inject

private const val SERVICE_TAG = "NotService"
private const val RECEIVER_TAG = "NotService.Receiver"

@AndroidEntryPoint
class NotificationService : Service() {

    @Inject
    lateinit var recognizeInteractor: RecognizeInteractor

    @Inject
    lateinit var trackRepository: TrackRepository

    @Inject
    @MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    @DefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    private val actionReceiver = ActionBroadcastReceiver()
    private val serviceScope by lazy { CoroutineScope(ioDispatcher + SupervisorJob()) }
    private val notificationManager get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private var notificationDeliveryJob: Job? = null

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        Log.d(SERVICE_TAG, "onCreate")
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFY_ID, createNotificationForStatus(RecognizeStatus.Ready))
        ContextCompat.registerReceiver(
            this,
            actionReceiver,
            IntentFilter().apply {
                addAction(RECOGNIZE_ACTION)
                addAction(CANCEL_ACTION)
                addAction(DISMISS_ACTION)
                addAction(ADD_TO_FAVORITE_ACTION)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        notificationDeliveryJob = serviceScope.launch {
            recognizeInteractor.statusFlow.collect { status ->
                notificationManager.notify(NOTIFY_ID, createNotificationForStatus(status))
            }
        }
    }

    private fun resetStatusToReady() = recognizeInteractor.resetStatusToReady()

    private fun cancelRecognition() = recognizeInteractor.cancelRecognize()

    private fun addResultTrackToFavs() {
        val track = when (val status = recognizeInteractor.statusFlow.value) {
            is RecognizeStatus.Success -> status.track
            else -> {
                showToast("Track not found")
                return
            }
        }
        serviceScope.launch {
            trackRepository.update(
                track.copy(
                    metadata = track.metadata.copy(isFavorite = !track.metadata.isFavorite)
                )
            )
            showToast("Added to favorites")
        }

    }

    // useless because toast is not visible if notification feed is expanded
    // should be replaced by pushing an intermediate message notification
    private fun showToast(text: String) {
        serviceScope.launch(mainDispatcher) {
            Toast.makeText(this@NotificationService, text, Toast.LENGTH_LONG).show()
        }
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


    private fun createNotificationChannel() {
        val name = getString(R.string.service_notification_chanel_name)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = getString(R.string.service_notification_chanel_description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            enableLights(false)
            enableVibration(true)
            vibrationPattern = longArrayOf(100, 100, 100, 100)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createMessageNotification(text: String): Notification {
        TODO()
    }

    private fun createNotificationForStatus(status: RecognizeStatus): Notification {

        val baseNotificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setAutoCancel(true)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_STATUS)
//            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))

        return when (status) {
            RecognizeStatus.Ready -> {
                baseNotificationBuilder
                    .setContentTitle("TuneSurfer")
                    .setContentText(getString(R.string.ready))
                    .addAction(
                        android.R.drawable.ic_btn_speak_now,
                        getString(R.string.recognize),
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(RECOGNIZE_ACTION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .build()
            }
            RecognizeStatus.Listening -> {
                baseNotificationBuilder
                    .setContentTitle(getString(R.string.listening))
                    .addAction(
                        android.R.drawable.ic_btn_speak_now,
                        getString(R.string.cancel),
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(CANCEL_ACTION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .build()
            }
            RecognizeStatus.Recognizing -> {
                baseNotificationBuilder
                    .setContentTitle(getString(R.string.recognizing))
                    .addAction(
                        android.R.drawable.ic_btn_speak_now,
                        getString(R.string.cancel),
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(CANCEL_ACTION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .build()
            }
            is RecognizeStatus.Error -> {
                baseNotificationBuilder
                    .setContentTitle("Some error was occur, check logs")
                    .addAction(
                        android.R.drawable.ic_btn_speak_now,
                        "Dismiss",
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(DISMISS_ACTION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .build()
            }
            RecognizeStatus.NoMatches -> {
                baseNotificationBuilder
                    .setContentTitle(getString(R.string.no_matches_found))
                    .addAction(
                        android.R.drawable.ic_btn_speak_now,
                        "Dismiss",
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(DISMISS_ACTION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .build()
            }
            is RecognizeStatus.Success -> {
                val bitmap = try {
                    val inputStream = URL(status.track.links.artwork).openStream()
                    BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    Log.e(SERVICE_TAG, "Bitmap loading error", e)
                    BitmapFactory.decodeResource(resources, R.drawable.meddle_album_cover)
                }

                baseNotificationBuilder
                    .setOnlyAlertOnce(false)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                    )
                    .setContentTitle(status.track.title)
                    .setContentText("${status.track.artist} / ${status.track.album} / ${status.track.releaseDate?.year.toString()}")
                    .setContentIntent(createTrackDeepLinkIntent(status.track.mbId))
                    .addAction(
                        android.R.drawable.ic_btn_speak_now,
                        "Add to favs",
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(ADD_TO_FAVORITE_ACTION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .addAction(
                        android.R.drawable.ic_btn_speak_now,
                        "Dismiss",
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(DISMISS_ACTION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .addAction(
                        android.R.drawable.ic_btn_speak_now,
                        getString(R.string.new_recognize),
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(RECOGNIZE_ACTION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .build()

            }
        }
    }

    private fun launchRecognizeOrCancel() {
//        recognizeInteractor.fakeRecognize(serviceScope)
        recognizeInteractor.launchRecognizeOrCancel(serviceScope)
    }

    private fun createTrackDeepLinkIntent(mbId: String): PendingIntent {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Screen.Track.createDeepLink(mbId).toUri(),
            this,
            MainActivity::class.java
        )
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
                ADD_TO_FAVORITE_ACTION -> {
                    addResultTrackToFavs()
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "music_recognizer_notification"
        private const val NOTIFY_ID = 1

        private const val RECOGNIZE_ACTION = "com.mrsep.musicrecognizer.action.RECOGNIZE_ACTION"
        private const val CANCEL_ACTION = "com.mrsep.musicrecognizer.action.CANCEL_ACTION"
        private const val DISMISS_ACTION = "com.mrsep.musicrecognizer.action.DISMISS_ACTION"
        private const val ADD_TO_FAVORITE_ACTION =
            "com.mrsep.musicrecognizer.action.ADD_TO_FAVORITE_ACTION"

        fun Context.toggleNotificationService(shouldStart: Boolean) {
            if (shouldStart) {
                startForegroundService(Intent(this, NotificationService::class.java))
            } else {
                stopService(Intent(this, NotificationService::class.java))
            }
        }
    }


}