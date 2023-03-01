package com.mrsep.musicrecognizer.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.RecognizeInteractor
import com.mrsep.musicrecognizer.domain.RecognizeStatus
import com.mrsep.musicrecognizer.domain.model.RecognizeResult
import com.mrsep.musicrecognizer.presentation.MainActivity
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

    private val actionReceiver = ActionBroadcastReceiver()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
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

    private fun resetRecognizer() = recognizeInteractor.resetRecognizer()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SERVICE_TAG, "onStartCommand: flags=$flags, startId=$startId")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(SERVICE_TAG, "onDestroy")
        notificationDeliveryJob?.cancel()
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
            RecognizeStatus.Failure -> {
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
            is RecognizeStatus.Success -> {

                if (status.result is RecognizeResult.Success) {

                    val bitmap = try {
                        val inputStream = URL(status.result.data.links.artwork).openStream()
                        BitmapFactory.decodeStream(inputStream)
                    } catch (e: Exception) {
                        Log.e(SERVICE_TAG,"Bitmap loading error", e)
                        BitmapFactory.decodeResource(resources, R.drawable.meddle_album_cover)
                    }

                    baseNotificationBuilder
                        .setOnlyAlertOnce(false)
                        .setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                        )
                        .setContentTitle(status.result.data.title)
                        .setContentText("${status.result.data.artist} / ${status.result.data.album} / ${status.result.data.releaseDate?.year.toString()}")
                        .setContentIntent(createTrackDeepLinkIntent(status.result.data.mbId))
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
                } else {
                    baseNotificationBuilder
                        .setContentTitle("Illegal state")
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
            "https://www.mrsep.com/track/$mbId".toUri(),
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
                    launchRecognizeOrCancel()
                }
                DISMISS_ACTION -> {
                    resetRecognizer()
                }
                ADD_TO_FAVORITE_ACTION -> {
                    // not implemented
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
        private const val ADD_TO_FAVORITE_ACTION = "com.mrsep.musicrecognizer.action.ADD_TO_FAVORITE_ACTION"

        fun Context.setExampleServiceEnabled(value: Boolean) {
            if (value) {
                startForegroundService(Intent(this, NotificationService::class.java))
            } else {
                stopService(Intent(this, NotificationService::class.java))
            }
        }

        fun Context.startExampleService() =
            startForegroundService(Intent(this, NotificationService::class.java))

        fun Context.stopExampleService() =
            stopService(Intent(this, NotificationService::class.java))
    }


}