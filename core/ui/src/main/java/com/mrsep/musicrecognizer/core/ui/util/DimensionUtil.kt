package com.mrsep.musicrecognizer.core.ui.util

import android.content.Context
import androidx.annotation.Px

fun Context.pxToDp(@Px px: Float): Float {
    return px / resources.displayMetrics.density
}

fun Context.dpToPx(dp: Float): Float {
    return dp * resources.displayMetrics.density
}
