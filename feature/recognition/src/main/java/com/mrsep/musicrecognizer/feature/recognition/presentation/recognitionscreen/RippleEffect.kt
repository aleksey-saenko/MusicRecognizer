package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun RippleEffect(
    activated: Boolean,
    modifier: Modifier = Modifier,
    startOffsetFraction: Float = 0.5f,
    circlesCount: Int = 7,
    animationSpeed: Int = 10_000,
    baseColor: Color = MaterialTheme.colorScheme.primary,
    activatedColor: Color = MaterialTheme.colorScheme.primary,
    fadeEasing: Easing = LinearEasing,
    scaleEasing: Easing = LinearEasing
) {
    val currentColor by animateColorAsState(
        targetValue = if (activated) activatedColor else baseColor,
        animationSpec = tween(durationMillis = 300),
        label = "currentColor"
    )

    val scaleFactor by animateFloatAsState(
        targetValue = if (activated) 1.25f else 1f,
        animationSpec = tween(durationMillis = 2000),
        label = "scaleFactor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "rippleTransition")
    val globalProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            // The core timeline remains linear
            animation = tween(durationMillis = animationSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "globalProgress"
    )

    Canvas(modifier = modifier) {
        val maxRadius = (size.minDimension / 2.0f) * scaleFactor

        for (circleIndex in 0 until circlesCount) {
            val phaseOffset = circleIndex.toFloat() / circlesCount
            val circleProgress = (globalProgress + phaseOffset) % 1f

            val scaledProgress = scaleEasing.transform(circleProgress)
            val actualRadiusFraction = startOffsetFraction + scaledProgress * (1f - startOffsetFraction)

            val fadeAmount = fadeEasing.transform(circleProgress)
            val alpha = (1f - fadeAmount).coerceIn(0f, 1f)

            drawCircle(
                color = currentColor,
                radius = actualRadiusFraction * maxRadius,
                alpha = alpha
            )
        }
    }
}
