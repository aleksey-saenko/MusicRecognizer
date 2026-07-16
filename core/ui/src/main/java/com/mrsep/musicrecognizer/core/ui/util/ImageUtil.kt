package com.mrsep.musicrecognizer.core.ui.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import com.materialkolor.ktx.fixIfDisliked
import com.materialkolor.score.Score

// Default mResizeArea = 12544 (112 x 112), so high-res input bitmap is unnecessary
fun Bitmap.generateThemeSeedColor(): Int? {
    val colorsToPopulation = Palette.from(this)
        .maximumColorCount(32)
        .generate()
        .swatches
        .ifEmpty { return null }
        .associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return rankedColors
        .first()
        .run(::Color)
        .fixIfDisliked()
        .toArgb()
}
