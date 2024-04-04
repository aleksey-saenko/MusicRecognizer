package com.mrsep.musicrecognizer.feature.onboarding.presentation.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun PageIndicator(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    spacing: Dp = indicatorSize,
    selectedMultiplier: Int = 3
) {
    assert(
        value = currentPage in 0 until totalPages,
        lazyMessage = { "Current page index is out of range." }
    )
    val rowWidth = (indicatorSize * (selectedMultiplier + (totalPages - 1))) +
            (spacing * (totalPages - 1))

    Row(
        modifier = modifier
            .requiredWidth(rowWidth),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until totalPages) {
            val selected = i == currentPage

            val width by animateDpAsState(
                targetValue = if (selected) indicatorSize * selectedMultiplier else indicatorSize,
                label = "WidthSize"
            )

            Canvas(
                modifier = Modifier
                    .size(width = width, height = indicatorSize),
                onDraw = {
                    drawRoundRect(
                        color = color,
                        cornerRadius = CornerRadius(indicatorSize.toPx() / 2),
                        size = Size(width = width.toPx(), height = indicatorSize.toPx())
                    )
                }
            )
        }
    }
}