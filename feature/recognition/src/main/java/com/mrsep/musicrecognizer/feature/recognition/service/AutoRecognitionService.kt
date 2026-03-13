package com.mrsep.musicrecognizer.feature.recognition.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioCaptureConfig
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioRecordingControllerFactory
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionInteractor
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val TAG = "AutoRecognitionService"
private const val NOTIFICATION_CHANNEL_ID = "com.mrsep.musicrecognizer.auto_recognition"
private const val NOTIFICATION_CHANNEL_ID_RESULT = "com.mrsep.musicrecognizer.auto_recognition_result"
private const val NOTIFICATION_ID = 100
private const val NOTIFICATION_ID_SUMMARY = 101
private const val NOTIFICATION_GROUP_KEY = "com.mrsep.musicrecognizer.AUTO_RECOGNIZE_GROUP"
private const val RECOGNITION_INTERVAL_MS = 10000L

@AndroidEntryPoint
class AutoRecognitionService : Service() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var recognitionInteractor: RecognitionInteractor

    @Inject
    lateinit var audioRecordingControllerFactory: AudioRecordingControllerFactory

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }
    private var recognitionJob: Job? = null
    private var lastRecognizedTrackId: String? = null
    private var notificationIdCounter = 1000
    private var recognizedSongsCount = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startAutoRecognition()
            ACTION_STOP -> stopAutoRecognition()
        }
        return START_STICKY
    }

    private fun startAutoRecognition() {
        try {
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MICROPHONE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot start foreground service - missing permissions", e)
            stopSelf()
            return
        }

        isRunning = true

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                applicationContext,
                getString(StringsR.string.auto_recognize_toast_on),
                Toast.LENGTH_SHORT
            ).show()
        }

        AutoRecognitionTileService.requestListeningState(this)

        recognitionJob = serviceScope.launch {
            while (isActive) {
                try {
                    performRecognition()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during recognition", e)
                }
                delay(RECOGNITION_INTERVAL_MS)
            }
        }
    }

    private suspend fun performRecognition() {
        Log.d(TAG, "Starting recognition cycle")

        recognitionInteractor.cancelAndJoin()

        val userPreferences = preferencesRepository.userPreferencesFlow.first()
        val captureConfig = when (userPreferences.defaultAudioCaptureMode) {
            AudioCaptureMode.Microphone -> AudioCaptureConfig.Microphone
            AudioCaptureMode.Device -> AudioCaptureConfig.Device(null)
            AudioCaptureMode.Auto -> AudioCaptureConfig.Auto(null)
            AudioCaptureMode.AutoRecognizer -> AudioCaptureConfig.Auto(null)
        }
        val audioController = audioRecordingControllerFactory.getAudioController(captureConfig)

        with(serviceScope) {
            recognitionInteractor.launchRecognition(audioController)
        }

        recognitionInteractor.status.transformWhile { status ->
            Log.d(TAG, "Status update: $status")
            emit(status)
            status !is RecognitionStatus.Done
        }.collect { status ->
            if (status is RecognitionStatus.Done) {
                handleRecognitionResult(status.result)
                recognitionInteractor.resetFinalStatus()
            }
        }
        Log.d(TAG, "Recognition cycle completed")
    }

    private suspend fun handleRecognitionResult(result: RecognitionResult) {
        when (result) {
            is RecognitionResult.Success -> {
                val trackId = result.track.id
                Log.d(TAG, "Recognition success: ${result.track.title} by ${result.track.artist}")
                if (trackId != lastRecognizedTrackId) {
                    lastRecognizedTrackId = trackId
                    showSongNotification(result.track)
                }
            }
            is RecognitionResult.Error -> {
                Log.e(TAG, "Recognition error: ${result.remoteError}")
            }
            is RecognitionResult.NoMatches -> {
                Log.d(TAG, "Recognition result: NoMatches")
            }
            is RecognitionResult.ScheduledOffline -> {
                Log.d(TAG, "Recognition result: ScheduledOffline")
            }
        }
    }

    private suspend fun showSongNotification(track: Track) {
        recognizedSongsCount++
        val notificationId = notificationIdCounter++
        val timestamp = System.currentTimeMillis()

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_RESULT)
            .setSmallIcon(UiR.drawable.ic_notification_ready)
            .setContentTitle(track.title)
            .setContentText(track.artist)
            .setAutoCancel(true)
            .setWhen(timestamp)
            .setShowWhen(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)

        track.artworkThumbUrl?.let { artworkUrl ->
            try {
                val imageLoader = ImageLoader(this)
                val request = ImageRequest.Builder(this)
                    .data(artworkUrl)
                    .size(Size(128, 128))
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                result.image?.toBitmap()?.let { bitmap ->
                    notificationBuilder.setLargeIcon(bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load artwork", e)
            }
        }

        notificationManager.notify(notificationId, notificationBuilder.build())

        val songsText = getString(StringsR.string.auto_recognize_songs_recognized, recognizedSongsCount)
        val summaryNotification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_RESULT)
            .setSmallIcon(UiR.drawable.ic_auto_recognize)
            .setContentTitle(getString(StringsR.string.app_name))
            .setContentText(songsText)
            .setWhen(timestamp)
            .setShowWhen(false)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setAutoCancel(false)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.InboxStyle().setSummaryText(songsText))
            .build()

        notificationManager.notify(NOTIFICATION_ID_SUMMARY, summaryNotification)
    }

    private fun stopAutoRecognition() {
        isRunning = false

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                applicationContext,
                getString(StringsR.string.auto_recognize_toast_off),
                Toast.LENGTH_SHORT
            ).show()
        }
        recognitionJob?.cancel()
        recognitionJob = null
        lastRecognizedTrackId = null
        notificationIdCounter = 1000
        recognizedSongsCount = 0

        serviceScope.launch {
            preferencesRepository.setAutoRecognizeEnabled(false)
        }

        AutoRecognitionTileService.requestListeningState(this)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(StringsR.string.auto_recognize_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(StringsR.string.auto_recognize_notification_channel_desc)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(serviceChannel)

        val resultChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID_RESULT,
            getString(StringsR.string.auto_recognize_result_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(StringsR.string.auto_recognize_result_channel_desc)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(resultChannel)
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, AutoRecognitionService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deleteIntent = Intent(this, AutoRecognitionService::class.java).apply {
            action = ACTION_STOP
        }
        val deletePendingIntent = PendingIntent.getService(
            this, 1, deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(UiR.drawable.ic_auto_recognize)
            .setContentTitle(getString(StringsR.string.auto_recognize_notification_title))
            .setContentText(getString(StringsR.string.auto_recognize_notification_text))
            .setOngoing(false)
            .setDeleteIntent(deletePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(StringsR.string.cancel),
                stopPendingIntent
            )
            .build()
    }

    companion object {
        const val ACTION_START = "com.mrsep.musicrecognizer.AUTO_RECOGNIZE_START"
        const val ACTION_STOP = "com.mrsep.musicrecognizer.AUTO_RECOGNIZE_STOP"

        @Volatile
        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, AutoRecognitionService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, AutoRecognitionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
