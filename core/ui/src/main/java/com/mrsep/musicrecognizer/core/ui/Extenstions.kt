package com.mrsep.musicrecognizer.core.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.ActivityCompat

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Should be called in the context of an Activity")
}

fun Activity.shouldShowRationale(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}