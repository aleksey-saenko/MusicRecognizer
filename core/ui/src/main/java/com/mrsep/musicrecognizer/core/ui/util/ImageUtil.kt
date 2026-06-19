package com.mrsep.musicrecognizer.core.ui.util

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

// Default mResizeArea = 12544 (112 x 112), so high-res input bitmap is unnecessary
fun Bitmap.getDominantColor(): Int? {
    return Palette.Builder(this)
        .maximumColorCount(24)
        .generate()
        .dominantSwatch?.rgb
}
