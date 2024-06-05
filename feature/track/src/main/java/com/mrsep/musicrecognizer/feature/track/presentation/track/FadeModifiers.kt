package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

internal fun Modifier.rowFadingEdge(
    isVisibleStartEdge: Boolean,
    isVisibleEndEdge: Boolean,
    startEdgeInitialColor: Color,
    startEdgeTargetColor: Color = Color.Transparent,
    endEdgeInitialColor: Color = startEdgeTargetColor,
    endEdgeTargetColor: Color = startEdgeInitialColor,
    fadeStartEdgeLengthDp: Dp,
    fadeEndEdgeLengthDp: Dp
) = this.then(
    composed {
        val density = LocalDensity.current
        val fadeStartEdgeLength = with(density) { fadeStartEdgeLengthDp.toPx() }
        val fadeEndEdgeLength = with(density) { fadeEndEdgeLengthDp.toPx() }

        val startEdgeLengthAnim = animateFloatAsState(
            targetValue = if (isVisibleStartEdge) fadeStartEdgeLength else 0.1f,
            animationSpec = tween(),
            label = "startEdgeLengthAnim"
        )
        val endEdgeLengthAnim = animateFloatAsState(
            targetValue = if (isVisibleEndEdge) fadeEndEdgeLength else 0.1f,
            animationSpec = tween(),
            label = "endEdgeLengthAnim"
        )

        drawWithContent {
            val startEdgeColors = listOf(startEdgeInitialColor, startEdgeTargetColor)
            val endEdgeColors = listOf(endEdgeInitialColor, endEdgeTargetColor)
            drawContent()
            if (isVisibleStartEdge) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        startEdgeColors,
                        endX = startEdgeLengthAnim.value
                    )
                )
            }
            if (isVisibleEndEdge) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        endEdgeColors,
                        startX = size.width - endEdgeLengthAnim.value,
                        endX = size.width
                    )
                )
            }
        }
    }
)

internal fun Modifier.rowFadingEdge(
    startEdgeInitialColor: Color,
    startEdgeTargetColor: Color = Color.Transparent,
    endEdgeInitialColor: Color = startEdgeTargetColor,
    endEdgeTargetColor: Color = startEdgeInitialColor,
    fadeStartEdgeLengthDp: Dp,
    fadeEndEdgeLengthDp: Dp
) = this.then(
    composed {
        val density = LocalDensity.current
        val fadeStartEdgeLength = with(density) { fadeStartEdgeLengthDp.toPx() }
        val fadeEndEdgeLength = with(density) { fadeEndEdgeLengthDp.toPx() }

        drawWithContent {
            val startEdgeColors = listOf(startEdgeInitialColor, startEdgeTargetColor)
            val endEdgeColors = listOf(endEdgeInitialColor, endEdgeTargetColor)
            drawContent()
            drawRect(
                brush = Brush.horizontalGradient(
                    startEdgeColors,
                    endX = fadeStartEdgeLength
                )
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    endEdgeColors,
                    startX = size.width - fadeEndEdgeLength,
                    endX = size.width
                )
            )
        }
    }
)
