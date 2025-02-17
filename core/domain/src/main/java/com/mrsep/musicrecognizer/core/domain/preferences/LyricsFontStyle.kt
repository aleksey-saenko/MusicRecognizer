package com.mrsep.musicrecognizer.core.domain.preferences

data class LyricsFontStyle(
    val fontSize: FontSize,
    val isBold: Boolean,
    val isHighContrast: Boolean,
    val alignToStart: Boolean,
)
