package com.mrsep.musicrecognizer

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlService
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlActivity
import com.mrsep.musicrecognizer.presentation.MainActivity
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@HiltAndroidApp
class MusicRecognizerApp : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject
    lateinit var okHttpClient: Provider<OkHttpClient>

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        RecognitionControlService.createStatusNotificationChannel(this)
        RecognitionControlService.createResultNotificationChannel(this)
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
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

    private fun getShortcuts() = listOf(
        recognitionShortcut(),
        backgroundRecognitionShortcut(),
    )
        .also { check(it.size <= 4) { "The guideline recommends publishing only 4 distinct shortcuts" } }
        .take(ShortcutManagerCompat.getMaxShortcutCountPerActivity(this))

    private fun recognitionShortcut(): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(this, SHORTCUT_ID_OPEN_AND_RECOGNIZE)
            .setShortLabel(getString(StringsR.string.app_shortcut_open_and_recognize_short))
            .setLongLabel(getString(StringsR.string.app_shortcut_open_and_recognize_long))
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_recognize))
            .setIntent(
                Intent(MainActivity.ACTION_RECOGNIZE, Uri.EMPTY, this, MainActivity::class.java)
            )
            .build()
    }

    private fun backgroundRecognitionShortcut(): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(this, SHORTCUT_ID_RECOGNIZE_IN_BACKGROUND)
            .setShortLabel(getString(StringsR.string.app_shortcut_recognize_short))
            .setLongLabel(getString(StringsR.string.app_shortcut_recognize_long))
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_recognize))
            .setIntent(
                Intent(RecognitionControlService.ACTION_LAUNCH_RECOGNITION, Uri.EMPTY, this, RecognitionControlActivity::class.java)
            )
            .build()
    }

    companion object {
        private const val SHORTCUT_ID_OPEN_AND_RECOGNIZE = "RecognizeShortcutId"
        private const val SHORTCUT_ID_RECOGNIZE_IN_BACKGROUND = "RecognizeInBackgroundShortcutId"
    }
}
