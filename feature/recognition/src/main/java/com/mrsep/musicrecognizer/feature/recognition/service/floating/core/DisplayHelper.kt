package com.mrsep.musicrecognizer.feature.recognition.service.floating.core

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager

internal class DisplayHelper internal constructor(
    private val context: Context,
    private val windowManager: WindowManager,
) {
    val metrics = DisplayMetrics()
    val safeInsets = Rect()

    init {
        refresh()
    }

    /** Updates cached display metrics and safe system insets. */
    fun refresh(decorView: View? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            val contextMetrics = context.resources.displayMetrics

            metrics.apply {
                widthPixels = bounds.width()
                heightPixels = bounds.height()
                density = contextMetrics.density
                densityDpi = contextMetrics.densityDpi
            }

            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            safeInsets.set(insets.left, insets.top, insets.right, insets.bottom)

        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay

            val realMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getRealMetrics(realMetrics)

            val appMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getMetrics(appMetrics)

            metrics.apply {
                widthPixels = realMetrics.widthPixels
                heightPixels = realMetrics.heightPixels
                density = realMetrics.density
                densityDpi = realMetrics.densityDpi
            }

            // Calculate the raw size of system decorations (like the navigation bar)
            val diffX = realMetrics.widthPixels - appMetrics.widthPixels
            val diffY = realMetrics.heightPixels - appMetrics.heightPixels

            // The status bar is always at the top, retrieve it from stable insets or fallback to 24dp
            @Suppress("DEPRECATION")
            val top = decorView?.rootWindowInsets?.stableInsetTop?.takeIf { it > 0 }
                ?: (24 * context.resources.displayMetrics.density).toInt()
            var left = 0
            var right = 0
            var bottom = 0

            @Suppress("DEPRECATION")
            val rotation = display.rotation
            // If there is a width discrepancy, the navigation bar is on the side (Landscape)
            if (diffX > 0) {
                if (rotation == Surface.ROTATION_270) {
                    left = diffX
                } else {
                    right = diffX
                }
            }

            // If there is a height discrepancy, the navigation bar is at the bottom (Portrait)
            if (diffY > 0) {
                bottom = diffY
            }

            safeInsets.set(left, top, right, bottom)
        }
    }
}
