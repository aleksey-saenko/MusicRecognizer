package com.mrsep.musicrecognizer.feature.recognition.service

import android.Manifest.permission
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.NotificationCompat
import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionTask
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.ui.util.dpToPx
import com.mrsep.musicrecognizer.feature.recognition.DeeplinkRouter
import com.mrsep.musicrecognizer.feature.recognition.service.ext.getCachedImageOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

class ResultNotificationHelper @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val deeplinkRouter: DeeplinkRouter,
) {

    private val notificationManager = appContext
        .getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

    fun cancelResultNotification() {
        notificationManager.cancel(NOTIFICATION_ID_RESULT)
    }

    suspend fun notifyResult(result: RecognitionResult) {
        if (appContext.isPostNotificationPermissionDenied()) return
        val notificationBuilder = when (result) {
            is RecognitionResult.Error -> {
                val (errorTitle, errorMessage) = when (result.remoteError) {
                    RemoteRecognitionResult.Error.BadConnection -> {
                        appContext.getString(StringsR.string.result_title_bad_connection) to
                                appContext.getString(StringsR.string.result_message_bad_connection)
                    }

                    is RemoteRecognitionResult.Error.BadRecording -> {
                        appContext.getString(StringsR.string.result_title_recording_error) to
                                appContext.getString(StringsR.string.result_message_recording_error)
                    }

                    RemoteRecognitionResult.Error.ApiUsageLimited -> {
                        appContext.getString(StringsR.string.result_title_service_usage_limited) to
                                appContext.getString(StringsR.string.result_message_service_usage_limited)
                    }

                    RemoteRecognitionResult.Error.AuthError -> {
                        appContext.getString(StringsR.string.result_title_auth_error) to
                                appContext.getString(StringsR.string.result_message_auth_error)
                    }

                    is RemoteRecognitionResult.Error.HttpError -> {
                        appContext.getString(StringsR.string.result_title_bad_network_response) to
                                appContext.getString(StringsR.string.result_message_bad_network_response)
                    }

                    is RemoteRecognitionResult.Error.UnhandledError -> {
                        appContext.getString(StringsR.string.result_title_internal_error) to
                                appContext.getString(StringsR.string.result_message_internal_error)
                    }
                }
                val bigText = result.recognitionTask.getMessage()?.let { scheduledTaskMessage ->
                    "$errorMessage\n$scheduledTaskMessage"
                } ?: errorMessage
                resultNotificationBuilder()
                    .setContentTitle(errorTitle)
                    .setContentText(errorMessage)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(errorTitle)
                            .bigText(bigText)
                    )
                    .addOptionalQueueScreenDeepLinkIntent(result.recognitionTask)
            }

            is RecognitionResult.ScheduledOffline -> {
                val (title, message) = when (val task = result.recognitionTask) {
                    is RecognitionTask.Created -> {
                        appContext.getString(StringsR.string.result_title_recognition_scheduled) to
                                if (task.launched)
                                    appContext.getString(StringsR.string.result_message_recognition_scheduled)
                                else
                                    appContext.getString(StringsR.string.result_message_recognition_saved)
                    }

                    is RecognitionTask.Error,
                    RecognitionTask.Ignored -> {
                        appContext.getString(StringsR.string.result_title_internal_error) to
                                appContext.getString(StringsR.string.result_message_internal_error)
                    }
                }
                resultNotificationBuilder()
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(title)
                            .bigText(message)
                    )
                    .addOptionalQueueScreenDeepLinkIntent(result.recognitionTask)
            }

            is RecognitionResult.NoMatches -> {
                val title = appContext.getString(StringsR.string.result_title_no_matches)
                val message = appContext.getString(StringsR.string.widget_tap_to_try_again)
                val bigText = result.recognitionTask.getMessage()?.let { scheduledTaskMessage ->
                    "$message\n$scheduledTaskMessage"
                } ?: message
                resultNotificationBuilder()
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(title)
                            .bigText(bigText)
                    )
                    .setContentIntent(
                        RecognitionControlActivity.startRecognitionWithPermissionRequestPendingIntent(appContext)
                    )
            }

            is RecognitionResult.Success -> {
                resultNotificationBuilder()
                    .setContentTitle(result.track.title)
                    .setContentText(result.track.artist)
                    .setExpandedStyleWithTrack(
                        artworkUrl = result.track.artworkUrl,
                        contentTitle = result.track.title,
                        contentText = result.track.artistWithAlbumFormatted()
                    )
                    .addTrackDeepLinkIntent(result.track.id)
                    .addOptionalShowLyricsButton(result.track)
                    .addShareButton(result.track.getSharedBody())
            }
        }
        notificationManager.notify(NOTIFICATION_ID_RESULT, notificationBuilder.build())
    }

    suspend fun notifyResult(enqueuedRecognition: EnqueuedRecognition) {
        if (appContext.isPostNotificationPermissionDenied()) return
        val track = when (val result = enqueuedRecognition.result) {
            is RemoteRecognitionResult.Success -> result.track
            else -> return
        }
        val contentTitle = enqueuedRecognition.title.ifBlank {
            val dateFormatted = enqueuedRecognition.creationDate.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            appContext.getString(StringsR.string.format_scheduled_recognition_of, dateFormatted)
        }
        val contentText = "${track.title} - ${track.artist}"

        val notification = NotificationCompat.Builder(
            appContext,
            NOTIFICATION_CHANNEL_ID_ENQUEUED_RESULT
        )
            .setSmallIcon(UiR.drawable.ic_notification_ready)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setOngoing(false)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setExpandedStyleWithTrack(
                artworkUrl = track.artworkUrl,
                contentTitle = contentTitle,
                contentText = track.title + "\n" + track.artistWithAlbumFormatted()
            )
            .addTrackDeepLinkIntent(track.id)
            .addOptionalShowLyricsButton(track)
            .addShareButton(track.getSharedBody())
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createPendingIntentForDeeplink(intent: Intent): PendingIntent {
        return TaskStackBuilder.create(appContext).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private suspend fun NotificationCompat.Builder.setExpandedStyleWithTrack(
        artworkUrl: String?,
        contentTitle: String,
        contentText: String,
    ): NotificationCompat.Builder {
        var bitmap: Bitmap? = null
        if (artworkUrl != null) {
            // Notification image should be ≤ 450dp wide, 2:1 aspect ratio
            val imageWidthPx = appContext.dpToPx(450f).toInt()
            val imageHeightPx = imageWidthPx / 2
            bitmap = appContext.getCachedImageOrNull(
                url = artworkUrl,
                widthPx = imageWidthPx,
                heightPx = imageHeightPx,
            )
        }
        val style = if (bitmap == null) {
            NotificationCompat.BigTextStyle()
                .setBigContentTitle(contentTitle)
                .bigText(contentText)
        } else {
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
        }
        return setStyle(style)
    }

    private fun NotificationCompat.Builder.addTrackDeepLinkIntent(
        trackId: String,
    ): NotificationCompat.Builder {
        val deepLinkIntent = deeplinkRouter.getDeepLinkIntentToTrack(trackId)
        val pendingIntent = createPendingIntentForDeeplink(deepLinkIntent)
        return setContentIntent(pendingIntent)
    }

    private fun NotificationCompat.Builder.addOptionalQueueScreenDeepLinkIntent(
        recognitionTask: RecognitionTask?,
    ): NotificationCompat.Builder {
        return when (recognitionTask) {
            is RecognitionTask.Created -> {
                val deepLinkIntent = deeplinkRouter.getDeepLinkIntentToQueue()
                val pendingIntent = createPendingIntentForDeeplink(deepLinkIntent)
                setContentIntent(pendingIntent)
            }

            is RecognitionTask.Error,
            RecognitionTask.Ignored,
            null -> this
        }
    }

    private fun NotificationCompat.Builder.addOptionalShowLyricsButton(
        track: Track,
    ): NotificationCompat.Builder {
        if (track.lyrics == null) return this
        val deepLinkIntent = deeplinkRouter.getDeepLinkIntentToLyrics(track.id)
        val pendingIntent = createPendingIntentForDeeplink(deepLinkIntent)
        return addAction(
            android.R.drawable.ic_menu_more,
            appContext.getString(StringsR.string.notification_button_show_lyrics),
            pendingIntent
        )
    }

    private fun NotificationCompat.Builder.addShareButton(
        sharedText: String,
    ): NotificationCompat.Builder {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sharedText)
        }
        val wrappedIntent = Intent.createChooser(intent, null)
        return addAction(
            android.R.drawable.ic_menu_share,
            appContext.getString(StringsR.string.share),
            PendingIntent.getActivity(
                appContext,
                0,
                wrappedIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun resultNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_RESULT)
            .setSmallIcon(UiR.drawable.ic_notification_ready)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setOngoing(false)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_MESSAGE)
    }


    private fun RecognitionTask.getMessage(): String? = when (this) {
        is RecognitionTask.Created -> if (launched)
            appContext.getString(StringsR.string.result_message_recognition_scheduled)
        else
            appContext.getString(StringsR.string.result_message_recognition_saved)

        is RecognitionTask.Error,
        RecognitionTask.Ignored -> null
    }

    private fun Track.artistWithAlbumFormatted(): String {
        val albumAndYear = album?.let { alb ->
            releaseDate?.year?.let { year -> "$alb ($year)" } ?: album
        }
        return albumAndYear?.let { albAndYear ->
            "$artist\n$albAndYear"
        } ?: artist
    }

    private fun Track.getSharedBody() = "$title - ${this.artistWithAlbumFormatted()}"

    companion object {
        private const val NOTIFICATION_ID_RESULT = 2

        private const val NOTIFICATION_CHANNEL_ID_RESULT = "com.mrsep.musicrecognizer.result"
        private const val NOTIFICATION_CHANNEL_ID_ENQUEUED_RESULT = "com.mrsep.musicrecognizer.enqueued_result"

        fun getChannelForRecognitionResults(context: Context): NotificationChannel {
            val name = context.getString(StringsR.string.notification_channel_name_result)
            val importance = NotificationManager.IMPORTANCE_HIGH
            return NotificationChannel(NOTIFICATION_CHANNEL_ID_RESULT, name, importance).apply {
                description = context.getString(StringsR.string.notification_channel_desc_result)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 100, 100, 100)
            }
        }

        fun getChannelForEnqueuedRecognitionResults(context: Context): NotificationChannel {
            val name = context.getString(StringsR.string.notification_channel_name_scheduled_result)
            val importance = NotificationManager.IMPORTANCE_HIGH
            return NotificationChannel(
                NOTIFICATION_CHANNEL_ID_ENQUEUED_RESULT,
                name,
                importance
            ).apply {
                description = context.getString(StringsR.string.notification_channel_desc_scheduled_result)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 100, 100, 100)
            }
        }
    }
}

internal fun Context.isPostNotificationPermissionDenied(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkSelfPermission(this, permission.POST_NOTIFICATIONS) == PERMISSION_DENIED
    } else {
        false
    }
}
