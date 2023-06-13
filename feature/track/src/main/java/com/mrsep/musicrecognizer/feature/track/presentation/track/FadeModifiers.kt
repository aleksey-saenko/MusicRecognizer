package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//val topBottomFade = Brush.verticalGradient(
//    0f to Color.Transparent,
//    0.3f to Color.Red,
//    0.7f to Color.Red,
//    1f to Color.Transparent
//)

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

fun Modifier.rowFadingEdge(
    isVisibleStartEdge: Boolean = true,
    isVisibleEndEdge: Boolean = true,
    startEdgeInitialColor: Color = Color.White,
    startEdgeTargetColor: Color = Color.Transparent,
    endEdgeInitialColor: Color = startEdgeTargetColor,
    endEdgeTargetColor: Color = startEdgeInitialColor,
    fadeStartEdgeLengthDp: Dp = 16.dp,
    fadeEndEdgeLengthDp: Dp = 16.dp,
) = this.then(
    composed {
        val density = LocalDensity.current
        val fadeStartEdgeLength = with(density) { fadeStartEdgeLengthDp.toPx() }
        val fadeEndEdgeLength = with(density) { fadeEndEdgeLengthDp.toPx() }

        val startEdgeLengthAnim = animateFloatAsState(
            targetValue = if (isVisibleStartEdge) fadeStartEdgeLength else 0.1f,
            animationSpec = tween()
        )
        val endEdgeLengthAnim = animateFloatAsState(
            targetValue = if (isVisibleEndEdge) fadeEndEdgeLength else 0.1f,
            animationSpec = tween()
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