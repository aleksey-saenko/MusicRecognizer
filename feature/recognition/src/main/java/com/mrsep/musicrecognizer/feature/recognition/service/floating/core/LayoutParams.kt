package com.mrsep.musicrecognizer.feature.recognition.service.floating.core

import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager

internal fun defaultLayoutParams() = WindowManager.LayoutParams().apply {
    height = WindowManager.LayoutParams.WRAP_CONTENT
    width = WindowManager.LayoutParams.WRAP_CONTENT
    format = PixelFormat.TRANSLUCENT
    gravity = Gravity.START or Gravity.TOP

    flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

    type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

//    windowAnimations = android.R.style.Animation_Translucent
    disableSystemMoveAnimations()
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        preferMinimalPostProcessing = true
//    }
}

private fun WindowManager.LayoutParams.disableSystemMoveAnimations() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        setCanPlayMoveAnimation(false)
    } else {
        disableSystemMoveAnimationsWithPrivateFlag()
    }
}

/**
 * Best-effort fallback for disabling system move animations before the public API was introduced.
 *
 * Source reference for the hidden flag:
 * https://android.googlesource.com/platform/frameworks/base/+/android-13.0.0_r1/core/java/android/view/WindowManager.java
 */
private fun WindowManager.LayoutParams.disableSystemMoveAnimationsWithPrivateFlag() {
    try {
        val layoutParamsClass = WindowManager.LayoutParams::class.java
        val privateFlagsField = layoutParamsClass.getField("privateFlags")
        val noMoveAnimationFlag = layoutParamsClass
            .getField("PRIVATE_FLAG_NO_MOVE_ANIMATION")
            .getInt(null)

        privateFlagsField.setInt(
            this,
            privateFlagsField.getInt(this) or noMoveAnimationFlag,
        )
    } catch (e: Exception) {
        Log.w("LayoutParams", "Failed to disable system move animations with private flag", e)
    }
}
