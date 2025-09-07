package com.mrsep.musicrecognizer

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.mrsep.musicrecognizer.feature.recognition.service.RecognitionControlActivity
import com.mrsep.musicrecognizer.feature.recognition.service.ResultNotificationHelper
import com.mrsep.musicrecognizer.feature.recognition.service.ServiceNotificationHelper
import com.mrsep.musicrecognizer.presentation.MainActivity
import dagger.hilt.android.HiltAndroidApp
import org.acra.ACRA
import org.acra.ACRAConstants
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import javax.inject.Inject
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@HiltAndroidApp
class MusicRecognizerApp : Application(), SingletonImageLoader.Factory, Configuration.Provider {

    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (ACRA.isACRASenderServiceProcess()) return
        setupAcra()
    }

    override fun onCreate() {
        super.onCreate()
        if (ACRA.isACRASenderServiceProcess()) return
        if (BuildConfig.DEBUG) enableStrictMode()
        createNotificationChannels()
        createShortcuts()
    }

    override fun newImageLoader(context: Context): ImageLoader = imageLoader.get()

    private fun createNotificationChannels() {
        getSystemService<NotificationManager>()?.createNotificationChannels(
            listOf(
                ServiceNotificationHelper.getChannelForRecognitionStatuses(this),
                ResultNotificationHelper.getChannelForBackgroundRecognitionResult(this),
                ResultNotificationHelper.getChannelForForegroundRecognitionResult(this),
                ResultNotificationHelper.getChannelForScheduledRecognitionResult(this),
            )
        )
    }

    private fun createShortcuts() {
        ShortcutManagerCompat.setDynamicShortcuts(this, getShortcuts())
    }

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
                RecognitionControlActivity.startRecognitionWithPermissionRequestIntent(this)
            )
            .build()
    }

    // Send reports only upon user approval (https://f-droid.org/en/docs/Anti-Features/#Tracking)
    private fun setupAcra() {
        ACRA.DEV_LOGGING = false
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            reportContent = ACRAConstants.DEFAULT_REPORT_FIELDS + ReportField.MEDIA_CODEC_LIST
            stopServicesOnCrash = true
            dialog {
                title = getString(StringsR.string.crash_dialog_title)
                resIcon = UiR.drawable.rounded_bug_report_fill1_24
                text = getString(StringsR.string.crash_dialog_message)
                commentPrompt = getString(StringsR.string.crash_dialog_comment_prompt)
                positiveButtonText = getString(StringsR.string.crash_dialog_button_send)
                negativeButtonText = getString(StringsR.string.crash_dialog_button_cancel)
                resTheme = R.style.Theme_MusicRecognizer_Dialog_Crash
            }
            mailSender {
                mailTo = "audile.crashes@gmail.com"
                reportAsFile = true
                reportFileName = "crash-report.json"
                subject = "Crash report"
            }
        }
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .apply {
                    detectLeakedSqlLiteObjects()
                    detectActivityLeaks()
                    detectLeakedClosableObjects()
                    detectLeakedRegistrationObjects()
                    detectFileUriExposure()
                    detectCleartextNetwork()
                    detectContentUriWithoutPermission()
//                    detectUntaggedSockets()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        detectCredentialProtectedWhileLocked()
                        detectImplicitDirectBoot()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        detectIncorrectContextUse()
                        detectUnsafeIntentLaunch()
                    }
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        detectBlockedBackgroundActivityLaunch()
                    }

                }
                .penaltyLog()
                .build()
        )
    }

    companion object {
        private const val SHORTCUT_ID_OPEN_AND_RECOGNIZE = "RecognizeShortcutId"
        private const val SHORTCUT_ID_RECOGNIZE_IN_BACKGROUND = "RecognizeInBackgroundShortcutId"
    }
}
