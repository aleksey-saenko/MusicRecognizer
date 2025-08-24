package com.mrsep.musicrecognizer.core.common.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

fun Context.getAppVersionName(removeDebug: Boolean = false): String? {
    val base = getPackageInfo().versionName
    return if (removeDebug) base?.substringBefore("-debug") else base
}

fun Context.getAppVersionCode(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    getPackageInfo().longVersionCode.toInt()
} else {
    @Suppress("DEPRECATION")
    getPackageInfo().versionCode
}


fun Context.getDefaultVibrator(): Vibrator {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}

private fun Context.getPackageInfo(): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }
}