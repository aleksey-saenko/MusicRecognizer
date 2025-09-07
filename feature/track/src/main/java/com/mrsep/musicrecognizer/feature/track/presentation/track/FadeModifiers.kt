package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

internal fun Modifier.horizontalFadingEdges(
    startFadeInitialColor: Color,
    startFadeTargetColor: Color = Color.Transparent,
    endFadeInitialColor: Color = startFadeTargetColor,
    endFadeTargetColor: Color = startFadeInitialColor,
    startFadeLength: Dp,
    endFadeLength: Dp = startFadeLength
) = this.then(
    Modifier.drawWithContent {
        val startLengthPx = startFadeLength.toPx()
        val endLengthPx = endFadeLength.toPx()
        drawContent()
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(startFadeInitialColor, startFadeTargetColor),
                endX = startLengthPx
            )
        )
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(endFadeInitialColor, endFadeTargetColor),
                startX = size.width - endLengthPx,
                endX = size.width
            )
        )
    }
)

internal fun Modifier.horizontalFadingEdges(
    isStartFadeVisible: Boolean,
    isEndFadeVisible: Boolean = isStartFadeVisible,
    animationSpec: AnimationSpec<Float> = spring(),
    startFadeInitialColor: Color,
    startFadeTargetColor: Color = Color.Transparent,
    endFadeInitialColor: Color = startFadeTargetColor,
    endFadeTargetColor: Color = startFadeInitialColor,
    startFadeLength: Dp,
    endFadeLength: Dp
) = composed {
    val density = LocalDensity.current
    val startLengthPx = with(density) { startFadeLength.toPx() }
    val endLengthPx = with(density) { endFadeLength.toPx() }
    val startLengthPxAnimated = animateFloatAsState(
        targetValue = if (isStartFadeVisible) startLengthPx else 0.1f,
        animationSpec = animationSpec,
        label = "startLengthPxAnimated"
    )
    val endLengthPxAnimated = animateFloatAsState(
        targetValue = if (isEndFadeVisible) endLengthPx else 0.1f,
        animationSpec = animationSpec,
        label = "endLengthPxAnimated"
    )
    drawWithContent {
        drawContent()
        if (isStartFadeVisible) {
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(startFadeInitialColor, startFadeTargetColor),
                    endX = startLengthPxAnimated.value
                )
            )
        }
        if (isEndFadeVisible) {
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(endFadeInitialColor, endFadeTargetColor),
                    startX = size.width - endLengthPxAnimated.value,
                    endX = size.width
                )
            )
        }
    }
}

internal fun Modifier.horizontalContentFading(
    startFadeLength: Dp,
    endFadeLength: Dp = startFadeLength
) = this.then(
    Modifier
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            val startLengthPx = startFadeLength.toPx()
            val endLengthPx = endFadeLength.toPx()
            drawContent()
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.Black),
                    endX = startLengthPx
                ),
                blendMode = BlendMode.DstIn,
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Black, Color.Transparent),
                    startX = size.width - endLengthPx,
                    endX = size.width
                ),
                blendMode = BlendMode.DstIn,
            )
        }
)

internal fun Modifier.verticalFadingEdges(
    topFadeInitialColor: Color,
    topFadeTargetColor: Color = Color.Transparent,
    bottomFadeInitialColor: Color = topFadeTargetColor,
    bottomFadeTargetColor: Color = topFadeInitialColor,
    topFadeLength: Dp,
    bottomFadeLength: Dp = topFadeLength
) = this.then(
    Modifier.drawWithContent {
        val topLengthPx = topFadeLength.toPx()
        val bottomLengthPx = bottomFadeLength.toPx()
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                listOf(topFadeInitialColor, topFadeTargetColor),
                startY = 0f,
                endY = topLengthPx
            ),
            topLeft = Offset(0f, 0f),
        )
        drawRect(
            brush = Brush.verticalGradient(
                listOf(bottomFadeInitialColor, bottomFadeTargetColor),
                startY = size.height - bottomLengthPx,
                endY = size.height
            ),
            topLeft = Offset(0f, size.height - bottomLengthPx)
        )
    }
)

internal fun Modifier.verticalFadingEdges(
    isTopFadeVisible: Boolean,
    isBottomFadeVisible: Boolean = isTopFadeVisible,
    animationSpec: AnimationSpec<Float> = spring(),
    topFadeInitialColor: Color,
    topFadeTargetColor: Color = Color.Transparent,
    bottomFadeInitialColor: Color = topFadeTargetColor,
    bottomFadeTargetColor: Color = topFadeInitialColor,
    topFadeLength: Dp,
    bottomFadeLength: Dp = topFadeLength
) = composed {
    val density = LocalDensity.current
    val topLengthPx = with(density) { topFadeLength.toPx() }
    val bottomLengthPx = with(density) { bottomFadeLength.toPx() }
    val topLengthPxAnimated = animateFloatAsState(
        targetValue = if (isTopFadeVisible) topLengthPx else 0.1f,
        animationSpec = animationSpec,
        label = "topLengthPxAnimated"
    )
    val bottomLengthPxAnimated = animateFloatAsState(
        targetValue = if (isBottomFadeVisible) bottomLengthPx else 0.1f,
        animationSpec = animationSpec,
        label = "bottomLengthPxAnimated"
    )
    drawWithContent {
        drawContent()
        if (isTopFadeVisible) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(topFadeInitialColor, topFadeTargetColor),
                    startY = 0f,
                    endY = topLengthPxAnimated.value
                ),
                topLeft = Offset(0f, 0f),
            )
        }
        if (isBottomFadeVisible) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(bottomFadeInitialColor, bottomFadeTargetColor),
                    startY = size.height - bottomLengthPxAnimated.value,
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height - bottomLengthPxAnimated.value)
            )
        }
    }
}

internal fun Modifier.verticalContentFading(
    topFadeLength: Dp,
    bottomFadeLength: Dp = topFadeLength
) = this.then(
    Modifier
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            val topLengthPx = topFadeLength.toPx()
            val bottomLengthPx = bottomFadeLength.toPx()
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topLengthPx
                ),
                blendMode = BlendMode.DstIn,
                topLeft = Offset.Zero,
                size = Size(size.width, topLengthPx)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - bottomLengthPx,
                    endY = size.height
                ),
                blendMode = BlendMode.DstIn,
                topLeft = Offset(0f, size.height - bottomLengthPx),
                size = Size(size.width, bottomLengthPx)
            )
        }
)
