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
internal fun RippleAnimated(
    activated: Boolean,
    modifier: Modifier = Modifier,
    startOffset: Float,
    circlesCount: Int = 5,
    animationSpeed: Int = 5_000,
    baseColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    activatedColor: Color = MaterialTheme.colorScheme.primary
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

    val infiniteTransition = rememberInfiniteTransition(label = "circleSizes")
    val circles = List(circlesCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = startOffset,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = animationSpeed,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(
                    offsetMillis = (animationSpeed / circlesCount) * (index + 1),
                    offsetType = StartOffsetType.FastForward
                )
            ),
            label = "circle#${index}Size"
        )
    }

    Canvas(
        modifier = modifier
    ) {
        circles.forEach { animatable ->
            drawCircle(
                color = currentColor,
                radius = animatable.value * size.minDimension / 2.0f * scaleFactor,
                alpha = 1 - animatable.value
            )
        }
    }
}
