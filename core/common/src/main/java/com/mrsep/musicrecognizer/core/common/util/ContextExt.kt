package com.mrsep.musicrecognizer.core.common.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

fun Context.getAppVersion(): String = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager?.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0)
        )?.versionName
    } else {
        @Suppress("DEPRECATION")
        packageManager?.getPackageInfo(
            packageName,
            0
        )?.versionName
    } ?: ""
} catch (e: PackageManager.NameNotFoundException) {
    ""
}

fun Context.getDefaultVibrator(): Vibrator {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}
