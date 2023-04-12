package com.mrsep.musicrecognizer.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale

@Composable
fun VinylAnimated(
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val enterTransition = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterTransition.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )
    }
    val degreesAnimated by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )

    )
    Canvas(
        modifier = modifier.alpha(enterTransition.value)
//            .border(width = 1.dp, color = Color.Green.copy(alpha = 0.1f))
    ) {
        val totalSize = size.minDimension
        val surfaceThickness = size.minDimension * 0.33f
        val surfaceRadius = (totalSize - surfaceThickness) / 2

        val arcThickness = totalSize * 0.025f
        fun getArcTopLeft(arcDiameter: Float) = Offset(
            x = (size.width - arcDiameter) / 2,
            y = (size.height - arcDiameter) / 2
        )
        scale(
            scale = enterTransition.value
        ) {
            drawCircle(
                //base layer
                color = color.copy(alpha = 0.1f),
                radius = totalSize / 2,
            )
            drawCircle( //record surface
                color = color.copy(alpha = 0.4f),
                radius = surfaceRadius,
                style = Stroke(
                    width = surfaceThickness
                )
            )
            drawCircle(
                //center dot
                color = color.copy(alpha = 0.3f),
                radius = totalSize / 25,
            )
            rotate(
                degrees = degreesAnimated
            ) {
                repeat(3) { index ->
                    val arcDiameter = size.minDimension * 1f - arcThickness * ((index + 1) * 5)
                    val arcTopLeft = getArcTopLeft(arcDiameter)

                    val arcParams = when (index) {
                        0 -> listOf(0f to 40f, 50f to 40f)
                        else -> listOf(0f to 90f)
                    }
                    arcParams.forEach { (startAngle, sweepAngle) ->
                        drawArc(
                            color = color.copy(alpha = 0.3f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = Size(width = arcDiameter, height = arcDiameter),
                            style = Stroke(
                                width = arcThickness,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }

                repeat(3) { index ->
                    val arcDiameter = size.minDimension * 1f - arcThickness * ((index + 1) * 5)
                    val arcTopLeft = getArcTopLeft(arcDiameter)

                    val arcParams = when (index) {
                        0 -> listOf(180f to 90f)
                        1 -> listOf(180f to 20f, 210f to 60f)
                        else -> listOf(180f to 60f, 250f to 20f)
                    }
                    arcParams.forEach { (startAngle, sweepAngle) ->
                        drawArc(
                            color = color.copy(alpha = 0.3f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = Size(width = arcDiameter, height = arcDiameter),
                            style = Stroke(
                                width = arcThickness,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }
        }

    }
}

//legacy variant
@Composable
fun VinylAnimatedNoBreaks(
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val enterTransition = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterTransition.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )
    }
    val degreesAnimated by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )

    )
    Canvas(
        modifier = modifier.alpha(enterTransition.value)
//            .border(width = 1.dp, color = Color.Green.copy(alpha = 0.1f))
    ) {
        val totalSize = size.minDimension
        val surfaceThickness = size.minDimension * 0.33f
        val surfaceRadius = (totalSize - surfaceThickness) / 2

        val arcThickness = totalSize * 0.025f
        fun getArcTopLeft(arcDiameter: Float) = Offset(
            x = (size.width - arcDiameter) / 2,
            y = (size.height - arcDiameter) / 2
        )
        scale(
            scale = enterTransition.value
        ) {
            drawCircle(
                //base layer
                color = color.copy(alpha = 0.1f),
                radius = totalSize / 2,
            )
            drawCircle( //record surface
                color = color.copy(alpha = 0.4f),
                radius = surfaceRadius,
                style = Stroke(
                    width = surfaceThickness
                )
            )
            drawCircle(
                //center dot
                color = color.copy(alpha = 0.3f),
                radius = totalSize / 25,
            )
            rotate(
                degrees = degreesAnimated
            ) {
                repeat(3) { index ->
                    val arcDiameter = size.minDimension * 1f - arcThickness * ((index + 1) * 5)
                    val arcTopLeft = getArcTopLeft(arcDiameter)
                    drawArc(
                        color = color.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = Size(width = arcDiameter, height = arcDiameter),
                        style = Stroke(
                            width = arcThickness,
                            cap = StrokeCap.Round
                        )
                    )
                }

                repeat(3) { index ->
                    val arcDiameter = size.minDimension * 1f - arcThickness * ((index + 1) * 5)
                    val arcTopLeft = getArcTopLeft(arcDiameter)
                    drawArc(
                        color = color.copy(alpha = 0.3f),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = Size(width = arcDiameter, height = arcDiameter),
                        style = Stroke(
                            width = arcThickness,
                            cap = StrokeCap.Round
                        )
                    )
                }
            }
        }

    }
}