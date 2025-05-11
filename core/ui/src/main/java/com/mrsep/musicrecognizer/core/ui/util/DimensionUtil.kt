package com.mrsep.musicrecognizer.core.ui.util

import android.content.Context
import androidx.core.util.TypedValueCompat

fun Context.pxToDp(px: Float): Float = TypedValueCompat.pxToDp(px, resources.displayMetrics)

fun Context.dpToPx(dp: Float): Float = TypedValueCompat.dpToPx(dp, resources.displayMetrics)

fun Context.spToPx(sp: Float): Float = TypedValueCompat.spToPx(sp, resources.displayMetrics)

fun Context.pxToSp(px: Float): Float = TypedValueCompat.pxToSp(px, resources.displayMetrics)
