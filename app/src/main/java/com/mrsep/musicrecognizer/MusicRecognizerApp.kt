package com.mrsep.musicrecognizer

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationService
import com.mrsep.musicrecognizer.presentation.MainActivity
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@HiltAndroidApp
class MusicRecognizerApp : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject
    lateinit var okHttpClient: Provider<OkHttpClient>

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        NotificationService.createStatusNotificationChannel(this)
        NotificationService.createResultNotificationChannel(this)
        ShortcutManagerCompat.setDynamicShortcuts(this, getShortcuts())
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient { okHttpClient.get() }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun getShortcuts() = listOf(
        recognizeShortcut(),
    )

    private fun recognizeShortcut(): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(this, RECOGNIZE_SHORTCUT_ID)
            .setShortLabel(getString(StringsR.string.quick_tile_recognize))
            .setLongLabel(getString(StringsR.string.quick_tile_recognize))
            .setIcon(IconCompat.createWithResource(this, UiR.drawable.ic_retro_microphone))
            .setIntent(
                Intent(ACTION_RECOGNIZE, Uri.EMPTY, this, MainActivity::class.java)
            )
            .build()
    }

    companion object {
        const val ACTION_RECOGNIZE = "com.mrsep.musicrecognizer.intent.action.RECOGNIZE"
        private const val RECOGNIZE_SHORTCUT_ID = "RecognizeShortcutId"
    }

}