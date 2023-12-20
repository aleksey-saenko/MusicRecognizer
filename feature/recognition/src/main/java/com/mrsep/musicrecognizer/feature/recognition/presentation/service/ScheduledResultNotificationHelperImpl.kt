package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import com.mrsep.musicrecognizer.feature.recognition.presentation.ext.artistWithAlbumFormatted
import com.mrsep.musicrecognizer.feature.recognition.presentation.ext.fetchBitmapOrNull
import com.mrsep.musicrecognizer.feature.recognition.presentation.ext.getSharedBody
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

internal class ScheduledResultNotificationHelperImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val serviceRouter: NotificationServiceRouter
) : ScheduledResultNotificationHelper {

    private val notificationManager get() =
            appContext.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

    private fun createEnqueuedResultNotificationChannel() {
        val name = appContext.getString(StringsR.string.notification_channel_name_scheduled_result)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(ENQUEUED_RESULT_CHANNEL_ID, name, importance).apply {
            description = appContext.getString(StringsR.string.notification_channel_desc_scheduled_result)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
            enableLights(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(100, 100, 100, 100)
        }
        notificationManager.createNotificationChannel(channel)
    }

    override suspend fun notify(enqueuedRecognition: EnqueuedRecognition) {
        createEnqueuedResultNotificationChannel()
        val track = when (enqueuedRecognition.result) {
            is RemoteRecognitionResult.Success -> enqueuedRecognition.result.track
            else -> return
        }
        val contentTitle = enqueuedRecognition.title.ifBlank {
            val dateFormatted = enqueuedRecognition.creationDate.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            appContext.getString(StringsR.string.format_scheduled_recognition_of, dateFormatted)
        }
        val contentText = "${track.title} - ${track.artistWithAlbumFormatted()}"

        val notification = NotificationCompat.Builder(appContext, ENQUEUED_RESULT_CHANNEL_ID)
            .setSmallIcon(UiR.drawable.ic_retro_microphone)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setOngoing(false)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .addOptionalBigPicture(track.artworkUrl)
            .addTrackDeepLinkIntent(track.id)
            .addShowLyricsButton(track)
            .addShareButton(track.getSharedBody())
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private suspend fun NotificationCompat.Builder.addOptionalBigPicture(
        url: String?
    ): NotificationCompat.Builder {
        return url?.let {
            appContext.fetchBitmapOrNull(url)?.let { bitmap ->
                setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                )
            }
        } ?: this
    }

    private fun createPendingIntent(intent: Intent): PendingIntent {
        return TaskStackBuilder.create(appContext).run {
            addNextIntentWithParentStack(intent)
            // FLAG_UPDATE_CURRENT to update trackId key on each result
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private fun NotificationCompat.Builder.addTrackDeepLinkIntent(
        trackId: String
    ): NotificationCompat.Builder {
        return setContentIntent(
            createPendingIntent(serviceRouter.getDeepLinkIntentToTrack(trackId))
        )
    }

    private fun NotificationCompat.Builder.addShowLyricsButton(
        track: Track
    ): NotificationCompat.Builder {
        if (track.lyrics == null) return this
        return addAction(
            android.R.drawable.ic_menu_more,
            appContext.getString(StringsR.string.show_lyrics),
            createPendingIntent(serviceRouter.getDeepLinkIntentToLyrics(track.id))
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
            appContext.getString(StringsR.string.share),
            PendingIntent.getActivity(
                appContext,
                0,
                wrappedIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    companion object {
        private const val ENQUEUED_RESULT_CHANNEL_ID = "com.mrsep.musicrecognizer.enqueued_result"
    }

}