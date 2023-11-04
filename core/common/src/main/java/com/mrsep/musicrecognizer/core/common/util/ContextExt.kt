package com.mrsep.musicrecognizer.core.common.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun Context.getAppVersion(): String = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager?.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0)
        )?.versionName
    } else {
        @Suppress("DEPRECATION") packageManager?.getPackageInfo(
            packageName,
            0
        )?.versionName
    } ?: ""
} catch (e: PackageManager.NameNotFoundException) {
    ""
}