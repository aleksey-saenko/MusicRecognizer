package com.mrsep.musicrecognizer.core.ui.util

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

fun Bitmap.getDominantColor(): Int? {
    return Palette.Builder(this)
        .maximumColorCount(24)
        .generate()
        .dominantSwatch?.rgb
}
