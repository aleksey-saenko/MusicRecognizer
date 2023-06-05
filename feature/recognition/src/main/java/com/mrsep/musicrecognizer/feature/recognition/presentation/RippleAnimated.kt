package com.mrsep.musicrecognizer.feature.recognition.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color

internal fun Modifier.drawBehindRippleAnimation(
    activated: Boolean,
//    amplitudeFactor: Float,
    startOffset: Float,
    circlesCount: Int,
    animationSpeed: Int,
    baseColor: Color,
    activatedColor: Color
) = composed {
    val currentColor by animateColorAsState(
        targetValue = if (activated) activatedColor else baseColor,
        animationSpec = tween(durationMillis = 300)
    )
    val scaleFactor by animateFloatAsState(
        targetValue = if (activated) 1.25f else 1f,
        animationSpec = tween(durationMillis = 2000)
    )

    val infiniteTransition = rememberInfiniteTransition()
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
            )
        )
    }
    drawBehind {
        // 1.45f makes it visible under button, should be fixed with adaptive sizes
        circles.forEach { animatable ->
            drawCircle(
                color = currentColor,
                radius = animatable.value * size.minDimension * 1.45f / 2.0f * scaleFactor,
                alpha = 1 - animatable.value,
//                style = Stroke(width = 4.dp.toPx()
            )
        }
    }
}

@Composable
internal fun RippleAnimated(
    activated: Boolean,
//    amplitudeFactor: Float,
    modifier: Modifier = Modifier,
    startOffset: Float,
    circlesCount: Int = 5,
    animationSpeed: Int = 5_000,
    baseColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    activatedColor: Color = MaterialTheme.colorScheme.primary
) {
//    val transition = updateTransition(activated, "Activated")
//    val currentColor by transition.animateColor(
//        transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
//        label = "Ripple Color",
//        targetValueByState = { if (it) activatedColor else baseColor }
//    )
//    val scaleFactor by transition.animateFloat(
//        transitionSpec = { spring(stiffness = Spring.StiffnessVeryLow) },
//        label = "Scale Factor",
//        targetValueByState = { if (it) 1.25f else 1f }
//    )
    val currentColor by animateColorAsState(
        targetValue = if (activated) activatedColor else baseColor,
        animationSpec = tween(durationMillis = 300)
    )
    val scaleFactor by animateFloatAsState(
        targetValue = if (activated) 1.25f else 1f,
        animationSpec = tween(durationMillis = 2000)
    )

    val infiniteTransition = rememberInfiniteTransition()
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
            )
        )
    }

    Canvas(
        modifier = modifier
    ) {
        circles.forEach { animatable ->
            drawCircle(
                color = currentColor,
                radius = animatable.value * size.minDimension / 2.0f * scaleFactor,
                alpha = 1 - animatable.value,
//                style = Stroke(width = 4.dp.toPx()
            )
        }
    }
}